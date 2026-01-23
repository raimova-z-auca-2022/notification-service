package com.example.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {

    private Long notificationId;

    private String recipient;
    private String subject;
    private String messageBody;

    private Integer statusId;
    private Integer retryCount;
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime updatedAt;
}
