package kg.notifications.gateway.messaging;

import kg.notifications.gateway.dto.NotificationCommandDto;
import kg.notifications.gateway.repository.ScheduledNotificationRepository;
import kg.notifications.gateway.service.ScheduledNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledMessageConsumer {

    private final ScheduledNotificationRepository repository;
    private final ScheduledNotificationService scheduledService;

    @RabbitListener(queues = "${app.rabbit.queue-scheduled-messages}")
    @Transactional
    public void processScheduledMessage(@Payload NotificationCommandDto command) {
        log.info("Processing scheduled message from queue: {}", command.notificationId());

        repository.findById(command.notificationId())
                .ifPresentOrElse(
                        entity -> {
                            if ("CANCELLED".equals(entity.status())) {
                                log.info("Scheduled message {} was cancelled", entity.id());
                                return;
                            }

                            boolean updated = repository.updateStatus(
                                    entity.id(),
                                    "SENT",
                                    LocalDateTime.now(),
                                    null
                            );

                            if (updated) {
                                scheduledService.sendToNotificationQueue(entity);
                                log.info("Scheduled message {} marked as SENT", entity.id());
                            } else {
                                log.warn("Failed to update status for scheduled message: {}", entity.id());
                            }
                        },
                        () -> log.error("Scheduled message not found in DB: {}", command.notificationId())
                );
    }
}