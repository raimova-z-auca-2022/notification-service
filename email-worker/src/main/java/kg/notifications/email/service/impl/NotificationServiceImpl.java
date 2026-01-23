package kg.notifications.email.service.impl;

import kg.notifications.email.dto.NotificationCommandDto;
import kg.notifications.email.entity.Notification;
import kg.notifications.email.enums.NotificationStatus;
import kg.notifications.email.repository.NotificationJdbcRepository;
import kg.notifications.email.service.AuditLogService;
import kg.notifications.email.service.EmailService;
import kg.notifications.email.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationJdbcRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    @Override
    @Transactional
    public void processEmailNotification(NotificationCommandDto cmd) {

        Notification notification = new Notification();
        notification.setExternalId(cmd.notificationId());
        notification.setChannelType("EMAIL");
        notification.setRecipient(cmd.recipient());
        notification.setMessageBody(cmd.text());
        notification.setSubject("Notification");
        notification.setStatusEnum(NotificationStatus.PENDING);
        notification.setStatusId(NotificationStatus.PENDING.getId());
        notification.setRetryCount(cmd.attempt());
        notification.setCreatedAt(cmd.createdAt() != null ? cmd.createdAt().toLocalDateTime() : LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        Long id = notificationRepository.insert(notification);
        notification.setNotificationId(id);

        log.info("Email notification {} saved as PENDING", cmd.notificationId());

        try {
            emailService.sendEmail(notification);

            notificationRepository.updateStatus(id, NotificationStatus.SENT, null, LocalDateTime.now());
            auditLogService.logSendEmail(notification, NotificationStatus.SENT.name(), null);

            log.info("Email notification {} successfully sent", cmd.notificationId());
            System.out.println("--------------------***********************--------------");

        } catch (Exception e) {

            log.error("Failed to send email notification {}", cmd.notificationId(), e);
            notificationRepository.updateStatus(id, NotificationStatus.FAILED, e.getMessage(), null);
            auditLogService.logSendEmail(notification, NotificationStatus.FAILED.name(), e.getMessage());

            throw e; // RabbitMQ retry / DLQ
        }
    }
}
