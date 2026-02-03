package kg.notifications.email.messaging;

import kg.notifications.email.dto.NotificationCommandDto;
import kg.notifications.email.service.EmailRetryService;
import kg.notifications.email.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final NotificationService notificationService;
    private final EmailRetryService<NotificationCommandDto> retryService;

    @RabbitListener(queues = "${app.rabbit.queueEmail}")
    public void handle(NotificationCommandDto cmd,
                       @Header(required = false, name = "x-retries-count") Integer retryCount,
                       @Header(name = AmqpHeaders.CORRELATION_ID, required = false) String correlationId) {
        
        if (cmd == null) {
            log.warn("Received null notification command");
            return;
        }
        
        log.debug("Received email notification {}, retryCount={}, correlationId={}", cmd.notificationId(), retryCount, correlationId);
        
        try {
            notificationService.processEmailNotification(cmd);
        } catch (Exception e) {
            log.error("Unhandled exception processing email for {}: {}", cmd.notificationId(), e.getMessage(), e);
            try {
                retryService.handleError(cmd, retryCount, e);
            } catch (Exception retryEx) {
                log.error("Failed to trigger retry for {}: {}", cmd.notificationId(), retryEx.getMessage(), retryEx);
            }
        }
    }
}
