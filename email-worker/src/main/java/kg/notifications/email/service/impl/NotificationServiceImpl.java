package kg.notifications.email.service.impl;

import kg.notifications.email.dto.NotificationCommandDto;
import kg.notifications.email.messaging.StatusEventPublisher;
import kg.notifications.email.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final StatusEventPublisher statusPublisher;

    @Override
    public void processEmailNotification(NotificationCommandDto cmd) {

        statusPublisher.publishProcessing(cmd);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(cmd.recipient());
            message.setSubject("Notification");
            message.setText(cmd.text());

            mailSender.send(message);

            statusPublisher.publishSent(cmd, null);

            log.info("EMAIL notification {} sent successfully", cmd.notificationId());

        } catch (Exception e) {

            statusPublisher.publishFailed(cmd, "EMAIL_SEND_ERROR", e.getMessage());

            log.error("EMAIL notification {} failed", cmd.notificationId(), e);

            throw e;
        }
    }
}
