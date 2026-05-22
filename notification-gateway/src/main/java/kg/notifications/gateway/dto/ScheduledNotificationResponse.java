package kg.notifications.gateway.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ScheduledNotificationResponse(
        String id,
        NotificationType type,
        String recipient,
        String text,
        OffsetDateTime scheduledAt,
        LocalDateTime createdAt,
        String status,
        LocalDateTime sentAt,
        String providerMessageId
) {}
