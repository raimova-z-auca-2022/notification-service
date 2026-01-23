package kg.notifications.email.service.impl;

import kg.notifications.email.entity.AuditLog;
import kg.notifications.email.entity.Notification;
import kg.notifications.email.repository.AuditLogRepository;
import kg.notifications.email.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logSendEmail(Notification notification,
                             String status,
                             String errorMessage) {

        AuditLog audit = new AuditLog();
        audit.setActionType("SEND_EMAIL");
        audit.setEntityType("NOTIFICATION");
        audit.setEntityId(notification.getNotificationId());

        StringBuilder details = new StringBuilder();
        details.append("Status=").append(status);
        if (errorMessage != null) {
            details.append(", error=").append(errorMessage);
        }
        audit.setDetails(details.toString());

        audit.setCreatedAt(LocalDateTime.now());

        auditLogRepository.insert(audit);
        log.debug("Audit log saved for notification {}", notification.getNotificationId());
    }
}
