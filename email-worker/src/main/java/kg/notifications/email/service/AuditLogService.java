package kg.notifications.email.service;

import kg.notifications.email.entity.Notification;

public interface AuditLogService {
    void logSendEmail(Notification notification, String status, String errorMessage);
}
