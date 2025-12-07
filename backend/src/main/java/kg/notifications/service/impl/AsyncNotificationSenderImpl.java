package kg.notifications.service.impl;

import kg.notifications.entity.Notification;
import kg.notifications.repository.NotificationJdbcRepository;
import kg.notifications.service.AsyncNotificationSender;
import kg.notifications.service.AuditLogService;
import kg.notifications.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncNotificationSenderImpl implements AsyncNotificationSender {

    private final NotificationJdbcRepository notificationRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    @Async
    @Transactional
    public void sendAsync(Long notificationId) {
        log.info("Start async sending for notification {}", notificationId);

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            log.warn("Notification {} not found, skip sending", notificationId);
            return;
        }

        Notification notification = notificationOpt.get();

        String newStatus;
        String errorMessage = null;
        LocalDateTime sentAt = null;

        try {
            emailService.sendEmail(notification);

            newStatus = "SENT";
            sentAt = LocalDateTime.now();
            notification.setStatus(newStatus);
            notification.setSentAt(sentAt);
            notification.setUpdatedAt(LocalDateTime.now());
            notificationRepository.updateStatus(notificationId, newStatus, null, sentAt);

            log.info("Notification {} sent successfully", notificationId);
        } catch (Exception ex) {
            newStatus = "FAILED";
            errorMessage = ex.getMessage();
            notification.setStatus(newStatus);
            notification.setErrorMessage(errorMessage);
            notification.setUpdatedAt(LocalDateTime.now());

            notificationRepository.updateStatus(notificationId, newStatus, errorMessage, null);

            log.error("Failed to send notification {}: {}", notificationId, ex.getMessage(), ex);
        }

        auditLogService.logSendEmail(notification, newStatus, errorMessage, null);
    }
}
