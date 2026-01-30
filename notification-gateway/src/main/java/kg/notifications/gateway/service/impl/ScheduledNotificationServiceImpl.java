package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.*;
import kg.notifications.gateway.repository.ScheduledNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kg.notifications.gateway.service.ScheduledNotificationService;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledNotificationServiceImpl implements ScheduledNotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ScheduledNotificationRepository repository;
    private final AppProperties props;

    @Override
    @Transactional
    public ScheduledNotificationResponse scheduleNotification(ScheduledNotificationRequest request) {

        ScheduledNotificationEntity entity = ScheduledNotificationEntity.create(request);

        repository.save(entity);

        sendToDelayedQueue(entity);

        log.info("Scheduled notification saved: {}", entity.id());

        return mapToResponse(entity);
    }

    @Override
    public ScheduledNotificationResponse getStatus(String id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Scheduled message not found: " + id));
    }

    @Override
    public boolean cancelScheduledMessage(String id) {
        return repository.cancelScheduled(id);
    }

    @Override
    public List<ScheduledNotificationResponse> listPending() {
        return repository.findByStatusAndScheduledAtBefore("PENDING", LocalDateTime.now().plusYears(1))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void sendToNotificationQueue(ScheduledNotificationEntity entity) {
        NotificationCommandDto command = new NotificationCommandDto(
                entity.id(),
                entity.type(),
                entity.recipient(),
                entity.text(),
                0,
                OffsetDateTime.now()
        );

        String routingKey = getRoutingKey(entity.type());

        rabbitTemplate.convertAndSend(
                props.getRabbit().getExchangeNotification(),
                routingKey,
                command
        );

        log.info("Sent scheduled message {} to {} queue", entity.id(), routingKey);
    }

    // --- Внутренние методы и Fallback (не часть публичного интерфейса) ---

    private void sendToDelayedQueue(ScheduledNotificationEntity entity) {
        NotificationCommandDto command = new NotificationCommandDto(
                entity.id(),
                entity.type(),
                entity.recipient(),
                entity.text(),
                0,
                OffsetDateTime.now()
        );

        long delay = calculateDelay(entity.scheduledAt());

        rabbitTemplate.convertAndSend(
                props.getRabbit().getExchangeDelayed(),
                props.getRabbit().getRoutingScheduled(),
                command,
                message -> {
                    message.getMessageProperties()
                            .setHeader("x-delay", delay);
                    return message;
                }
        );

        log.debug("Sent to delayed queue: {} with delay {} ms", entity.id(), delay);
    }

    private long calculateDelay(OffsetDateTime scheduledAt) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (scheduledAt.isBefore(now)) return 0;
        return Duration.between(now, scheduledAt).toMillis();
    }

    private String getRoutingKey(NotificationType type) {
        return switch (type) {
            case EMAIL -> props.getRabbit().getRoutingEmail();
            case TELEGRAM -> props.getRabbit().getRoutingTelegram();
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    private ScheduledNotificationResponse mapToResponse(ScheduledNotificationEntity entity) {
        return new ScheduledNotificationResponse(
                entity.id(),
                entity.type(),
                entity.recipient(),
                entity.text(),
                entity.scheduledAt(),
                entity.createdAt(),
                entity.status()
        );
    }


    @Scheduled(fixedDelayString = "${scheduling.missed-check-delay:60000}")
    @Transactional
    public void checkMissedScheduledMessages() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkTime = now.minusMinutes(5);

        List<ScheduledNotificationEntity> missed = repository
                .findByStatusAndScheduledAtBefore("PENDING", checkTime);

        if (!missed.isEmpty()) {
            log.warn("Found {} missed scheduled messages", missed.size());

            missed.forEach(entity -> {
                repository.updateStatus(entity.id(), "MISSED", now);

                sendToNotificationQueue(entity);
            });
        }
    }
}
