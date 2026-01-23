package kg.notifications.gateway.dto;

import java.time.OffsetDateTime;

public record NotificationCommandDto(
        String notificationId,
        NotificationType type,
        String recipient,
        String text,
        int attempt,
        OffsetDateTime createdAt
) {}
