package kg.notifications.email.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLog {

    private Long id;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}

