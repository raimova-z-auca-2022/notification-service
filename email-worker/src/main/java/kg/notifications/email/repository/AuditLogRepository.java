package kg.notifications.email.repository;

import kg.notifications.email.entity.AuditLog;

public interface AuditLogRepository {

    void insert(AuditLog auditLog);
}
