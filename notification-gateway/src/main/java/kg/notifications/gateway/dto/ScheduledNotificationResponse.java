package kg.notifications.gateway.dto;

import java.time.LocalDateTime;

public record ScheduledNotificationResponse(
        String id,
        NotificationType type,
        String recipient,
        String text,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt,
        String status
) {}
