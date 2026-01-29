package kg.notifications.gateway.dto;

import java.time.OffsetDateTime;

public record NotificationResponse(
        String id,
        NotificationType type,
        String recipient,
        String text,
        NotificationStatus status,
        int attempt,
        String lastErrorCode,
        String lastErrorMessage,
        String providerMessageId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
