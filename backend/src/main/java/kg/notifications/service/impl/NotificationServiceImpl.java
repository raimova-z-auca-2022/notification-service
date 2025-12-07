package kg.notifications.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import kg.notifications.dto.SendNotificationRequest;
import kg.notifications.dto.SendNotificationResponse;
import kg.notifications.entity.Notification;
import kg.notifications.enums.NotificationStatus;
import kg.notifications.repository.NotificationJdbcRepository;
import kg.notifications.service.AsyncNotificationSender;
import kg.notifications.service.AuditLogService;
import kg.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationJdbcRepository notificationRepository;
    private final AsyncNotificationSender asyncNotificationSender;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SendNotificationResponse sendEmailNotification(SendNotificationRequest request,
                                                          HttpServletRequest httpRequest) {

        Notification notification = new Notification();
        notification.setRecipient(request.getRecipientEmail());
        notification.setSubject(request.getSubject());
        notification.setMessageBody(request.getBody());

        notification.setChannelType("EMAIL");
        notification.setStatusEnum(NotificationStatus.PENDING);
        notification.setRetryCount(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        Long id = notificationRepository.insert(notification);

        notification.setNotificationId(id);

        NotificationStatus status = NotificationStatus.PENDING;
        log.info("Notification {} saved with status {}", id, status.name());

        auditLogService.logSendEmail(notification, status.name(), null, httpRequest);

        asyncNotificationSender.sendAsync(id);

        return new SendNotificationResponse(
                id,
                status.name(),
                "Notification accepted for sending"
        );
    }

    @Override
    public Optional<Notification> getNotification(Long id) {
        return notificationRepository.findById(id);
    }
}
