package kg.notifications.gateway.messaging.listener;

import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.messaging.dto.StatusEventDto;
import kg.notifications.gateway.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatusUpdateListener {

    private final NotificationRepository repository;

    @RabbitListener(queues = "q.status")
    public void handleStatusUpdate(StatusEventDto event) {
        log.info("Processing status update for {}: {}", event.notificationId(), event.status());

        try {
            repository.updateFromStatusEvent(
                    UUID.fromString(event.notificationId()),
                    event.status(),
                    event.attempt(),
                    event.errorCode(),
                    event.errorMessage(),
                    event.providerMessageId()
            );
            log.info("Database updated for notification {}", event.notificationId());
        } catch (Exception e) {
            log.error("Failed to update status in DB: {}", e.getMessage());
        }
    }
}