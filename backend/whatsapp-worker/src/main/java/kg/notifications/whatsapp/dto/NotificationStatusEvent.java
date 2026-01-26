package kg.notifications.whatsapp.dto;

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
public class NotificationStatusEvent {
    private UUID notificationId;
    private String channel;
    private String status;
    private String messageId;
    private String errorMessage;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;

    // Геттеры
    public UUID getNotificationId() { return notificationId; }
    public String getStatus() { return status; }
    // ... остальные геттеры
}