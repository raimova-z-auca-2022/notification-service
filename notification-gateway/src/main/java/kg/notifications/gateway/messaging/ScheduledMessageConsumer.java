package kg.notifications.gateway.messaging;

import com.rabbitmq.client.Channel;
import kg.notifications.gateway.dto.NotificationCommandDto;
import kg.notifications.gateway.repository.ScheduledNotificationRepository;
import kg.notifications.gateway.service.ScheduledNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledMessageConsumer {

    private final ScheduledNotificationRepository repository;
    private final ScheduledNotificationService scheduledService;

    @RabbitListener(queues = "${app.rabbit.queue-scheduled-messages}")
    @Transactional
    public void processScheduledMessage(@Payload NotificationCommandDto command,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Processing scheduled message from queue: {}", command.notificationId());

        try {
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
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to process scheduled message {}: {}", command.notificationId(), e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }
}