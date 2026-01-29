package kg.notifications.gateway.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduledNotificationEntity(
        String id,
        NotificationType type,
        String recipient,
        String text,
        LocalDateTime scheduledAt,
        String status,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ScheduledNotificationEntity create(ScheduledNotificationRequest request) {
        return new ScheduledNotificationEntity(
                UUID.randomUUID().toString(),
                request.type(),
                request.recipient(),
                request.text(),
                request.scheduledAt(),
                "PENDING",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}