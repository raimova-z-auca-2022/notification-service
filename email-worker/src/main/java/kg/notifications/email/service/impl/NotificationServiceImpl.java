package kg.notifications.email.service.impl;

import kg.notifications.email.dto.NotificationCommandDto;
import kg.notifications.email.messaging.StatusEventPublisher;
import kg.notifications.email.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final StatusEventPublisher statusPublisher;

    @Override
    @Transactional
    public void processEmailNotification(NotificationCommandDto cmd) {
        log.debug("Processing email notification for {}", cmd.notificationId());
        
        try {
            statusPublisher.publishProcessing(cmd);
            
            SimpleMailMessage message = buildMailMessage(cmd);
            mailSender.send(message);
            
            statusPublisher.publishSent(cmd, null);
            log.info("EMAIL notification {} sent successfully", cmd.notificationId());
            
        } catch (Exception e) {
            log.error("EMAIL notification {} send failed: {}", cmd.notificationId(), e.getMessage(), e);
            try {
                statusPublisher.publishFailed(cmd, "EMAIL_SEND_ERROR", e.getMessage());
            } catch (Exception statusEx) {
                log.error("Failed to publish failure status for {}: {}", cmd.notificationId(), statusEx.getMessage(), statusEx);
            }
        }
    }
    
    private SimpleMailMessage buildMailMessage(NotificationCommandDto cmd) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(cmd.recipient());
        message.setSubject("Notification");
        message.setText(cmd.text());
        return message;
    }
}
