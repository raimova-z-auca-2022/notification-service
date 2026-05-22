package kg.notifications.sms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data  // ← УБЕДИТЕСЬ ЧТО ЭТО ЕСТЬ!
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsNotificationMessage {
    private UUID notificationId;

    @JsonProperty("text")
    private String message;

    private String recipient;
    private String templateId;
    private Map<String, String> templateVariables;
    private Integer priority;
    private String idempotencyKey;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Добавьте геттеры если нет Lombok
    public UUID getNotificationId() { return notificationId; }
    public String getMessage() { return message; }
    public String getRecipient() { return recipient; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getMaskedRecipient() {
        if (recipient == null || recipient.length() < 4) return "***";
        return recipient.substring(0, recipient.length() - 4) + "****";
    }

    public boolean isValid() {
        return notificationId != null &&
                message != null && !message.trim().isEmpty() &&
                recipient != null && !recipient.trim().isEmpty();
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        if (notificationId == null) errors.append("notificationId is required; ");
        if (message == null || message.trim().isEmpty()) errors.append("message/text is required; ");
        if (recipient == null || recipient.trim().isEmpty()) errors.append("recipient is required; ");
        return errors.toString();
    }
}