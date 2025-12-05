package kg.notifications.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Модель, соответствующая "большой" таблице public.notifications
 * на сервере. Мы используем только часть полей, остальные могут
 * оставаться null.
 */
@Data
public class Notification {

    private Long notificationId;

    private Integer clientId;

    private String channelType;

    private String recipient;

    private String subject;

    private String messageBody;

    private String status;

    private Integer retryCount;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;
}