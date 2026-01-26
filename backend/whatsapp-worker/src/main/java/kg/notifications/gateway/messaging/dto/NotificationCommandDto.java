package kg.notifications.gateway.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCommandDto implements Serializable {
    private String type; // Заменили NotificationType на обычный String
    private String recipient;
    private String text;
}