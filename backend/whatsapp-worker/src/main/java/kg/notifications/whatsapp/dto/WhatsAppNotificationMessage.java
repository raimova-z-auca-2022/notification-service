package kg.notifications.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppNotificationMessage {
    private UUID notificationId;
    private String recipient;
    private String message;
    private String templateId;
    private Map<String, String> templateVariables;
    private Integer priority;
    private String idempotencyKey;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isValid() {
        return notificationId != null &&
                recipient != null && !recipient.trim().isEmpty() &&
                message != null && !message.trim().isEmpty();
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        if (notificationId == null) errors.append("notificationId is required; ");
        if (recipient == null || recipient.trim().isEmpty()) errors.append("recipient is required; ");
        if (message == null || message.trim().isEmpty()) errors.append("message is required; ");
        return errors.toString();
    }

    public String getMaskedRecipient() {
        if (recipient == null || recipient.length() < 4) return "***";
        return recipient.substring(0, recipient.length() - 4) + "****";
    }
}