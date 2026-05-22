package kg.notifications.sms.dto;

import java.time.OffsetDateTime;

public record StatusEventDto(
        String notificationId,
        NotificationType type,
        NotificationStatus status,
        int attempt,
        String errorCode,
        String errorMessage,
        String providerMessageId,
        OffsetDateTime occurredAt
) {}
