package kg.notifications.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import kg.notifications.dto.SendNotificationRequest;
import kg.notifications.dto.SendNotificationResponse;
import kg.notifications.entity.Notification;
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
        notification.setStatus("PENDING");
        notification.setRetryCount(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        notificationRepository.insert(notification);
        Long id = notification.getNotificationId();

        log.info("Notification {} saved with status PENDING", id);

        auditLogService.logSendEmail(notification, "PENDING", null, httpRequest);

        asyncNotificationSender.sendAsync(id);

        return new SendNotificationResponse(
                id,
                "PENDING",
                "Notification accepted for sending"
        );
    }

    @Override
    public Optional<Notification> getNotification(Long id) {
        return notificationRepository.findById(id);
    }
}
