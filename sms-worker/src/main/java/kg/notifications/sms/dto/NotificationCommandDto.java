package kg.notifications.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationCommandDto(
        String notificationId,
        String type,
        String recipient,
        String text,
        int attempt,
        OffsetDateTime createdAt
) implements Serializable {}