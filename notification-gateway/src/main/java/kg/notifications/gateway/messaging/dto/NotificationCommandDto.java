package kg.notifications.gateway.messaging.dto;

import kg.notifications.gateway.dto.NotificationType;

import java.time.OffsetDateTime;

public record NotificationCommandDto(
        String notificationId,
        NotificationType type,
        String recipient,
        String text,
        int attempt,
        OffsetDateTime createdAt
) {}
