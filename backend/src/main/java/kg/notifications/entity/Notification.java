package kg.notifications.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {

    private Long notificationId;

    private Integer clientId;

    private String channelType;

    private String recipient;

    private String subject;

    private String messageBody;

    private Integer statusId;

    private Integer retryCount;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;


    public kg.notifications.enums.NotificationStatus getStatusEnum() {
        if (statusId == null) {
            return null;
        }
        return kg.notifications.enums.NotificationStatus.fromId(statusId);
    }

    public void setStatusEnum(kg.notifications.enums.NotificationStatus status) {
        if (status == null) {
            this.statusId = null;
        } else {
            this.statusId = status.getId();
        }
    }

}
