package kg.notifications.whatsapp.service;

import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
import org.springframework.amqp.core.Message;

public interface RetryService {
    void scheduleRetry(Message originalMessage, WhatsAppNotificationMessage notificationMessage, int nextRetryCount);
}