package kg.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendNotificationResponse {
    private Long notificationId;
    private String status;
    private String message;
}