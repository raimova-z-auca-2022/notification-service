package kg.notifications.service;

import jakarta.servlet.http.HttpServletRequest;
import kg.notifications.entity.AuditLog;
import kg.notifications.entity.Notification;
import kg.notifications.repository.AuditLogJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogJdbcRepository auditLogRepository;

    public void logSendEmail(Notification notification,
                             String status,
                             String errorMessage,
                             HttpServletRequest request) {

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

        if (request != null) {
            audit.setIpAddress(request.getRemoteAddr());
            audit.setUserAgent(request.getHeader("User-Agent"));
        }

        audit.setCreatedAt(LocalDateTime.now());

        auditLogRepository.insert(audit);
        log.debug("Audit log saved for notification {}", notification.getNotificationId());
    }
}
