package kg.notifications.email.entity;

import kg.notifications.email.enums.NotificationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {

    private Long notificationId;
    private String externalId;       // notificationId из очереди
    private Integer clientId;
    private String channelType;      // EMAIL
    private String recipient;
    private String subject;
    private String messageBody;
    private Integer statusId;
    private NotificationStatus statusEnum;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
}
