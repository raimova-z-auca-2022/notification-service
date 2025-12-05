package kg.notifications.service;

import jakarta.servlet.http.HttpServletRequest;
import kg.notifications.dto.SendNotificationRequest;
import kg.notifications.dto.SendNotificationResponse;
import kg.notifications.entity.Notification;
import kg.notifications.repository.NotificationJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationJdbcRepository notificationRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Transactional
    public SendNotificationResponse sendEmailNotification(SendNotificationRequest request,
                                                          HttpServletRequest httpRequest) {
        Notification notification = new Notification();
        notification.setClientId(null);
        notification.setChannelType("EMAIL");
        notification.setRecipient(request.getRecipientEmail());
        notification.setSubject(request.getSubject());
        notification.setMessageBody(request.getBody());
        notification.setStatus("PENDING");
        notification.setRetryCount(0);
        notification.setCreatedAt(LocalDateTime.now());

        Long id = notificationRepository.insert(notification);
        notification.setNotificationId(id);

        String status;
        String errorMessage = null;
        LocalDateTime sentAt = null;

        try {
            emailService.sendEmail(notification);
            status = "SENT";
            sentAt = LocalDateTime.now();
            notificationRepository.updateStatus(id, status, null, sentAt);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", notification.getRecipient(), ex.getMessage());
            status = "FAILED";
            errorMessage = ex.getMessage();
            notificationRepository.updateStatus(id, status, errorMessage, null);
        }

        notification.setStatus(status);
        notification.setErrorMessage(errorMessage);
        notification.setSentAt(sentAt);

        auditLogService.logSendEmail(notification, status, errorMessage, httpRequest);

        String message = status.equals("SENT")
                ? "Notification sent successfully"
                : "Failed to send notification";

        return new SendNotificationResponse(id, status, message);
    }

    public Optional<Notification> getNotification(Long id) {
        return notificationRepository.findById(id);
    }
}