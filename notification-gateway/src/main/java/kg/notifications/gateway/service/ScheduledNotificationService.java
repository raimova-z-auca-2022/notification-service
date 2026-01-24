package kg.notifications.gateway.service;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.*;
import kg.notifications.gateway.repository.ScheduledNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledNotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ScheduledNotificationRepository repository;
    private final AppProperties props;

    @Transactional
    public ScheduledNotificationResponse scheduleNotification(ScheduledNotificationRequest request) {
        // Создаем entity
        ScheduledNotificationEntity entity = ScheduledNotificationEntity.create(request);

        // Сохраняем в БД
        repository.save(entity);

        // Отправляем в delayed exchange
        sendToDelayedQueue(entity);

        log.info("Scheduled notification saved: {}", entity.id());

        return mapToResponse(entity);
    }

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

    private long calculateDelay(LocalDateTime scheduledTime) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime scheduled = scheduledTime.atZone(ZoneId.systemDefault());
        return Duration.between(now, scheduled).toMillis();
    }


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

    private String getRoutingKey(NotificationType type) {
        return switch (type) {
            case EMAIL -> props.getRabbit().getRoutingEmail();
            case TELEGRAM -> props.getRabbit().getRoutingTelegram();
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    // Fallback проверка
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
                // Помечаем как пропущенные
                repository.updateStatus(entity.id(), "MISSED", now);

                // Отправляем для обработки
                sendToNotificationQueue(entity);
            });
        }
    }

    public ScheduledNotificationResponse getStatus(String id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Scheduled message not found: " + id));
    }

    public boolean cancelScheduledMessage(String id) {
        return repository.cancelScheduled(id);
    }

    public List<ScheduledNotificationResponse> listPending() {
        return repository.findByStatusAndScheduledAtBefore("PENDING", LocalDateTime.now().plusYears(1))
                .stream()
                .map(this::mapToResponse)
                .toList();
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
}