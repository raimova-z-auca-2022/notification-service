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
        log.info("Received status update for {}: {}", event.getNotificationId(), event.getStatus());

        try {
            repository.updateFromStatusEvent(
                    UUID.fromString(event.getNotificationId()),
                    NotificationStatus.valueOf(event.getStatus()), // Превращаем String обратно в Enum
                    event.getAttempt(),
                    event.getErrorCode(),
                    event.getErrorMessage(),
                    event.getProviderMessageId()
            );
            log.info("Database updated for notification {}", event.getNotificationId());
        } catch (Exception e) {
            log.error("Failed to update status in DB: {}", e.getMessage());
        }
    }
}