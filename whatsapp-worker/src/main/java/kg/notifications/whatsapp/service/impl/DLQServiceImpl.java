package kg.notifications.whatsapp.service.impl;

import kg.notifications.whatsapp.config.AppProperties;
import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
import kg.notifications.whatsapp.service.DLQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DLQServiceImpl implements DLQService {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;

    @Override
    public void sendToDLQ(WhatsAppNotificationMessage message, String errorReason) {
        Map<String, Object> dlqMessage = new HashMap<>();
        dlqMessage.put("notificationId", message.getNotificationId().toString());
        dlqMessage.put("recipient", message.getMaskedRecipient());
        dlqMessage.put("originalMessage", message.getMessage());
        dlqMessage.put("errorReason", errorReason);
        dlqMessage.put("retryCount", message.getRetryCount());
        dlqMessage.put("timestamp", LocalDateTime.now().toString());

        try {
            rabbitTemplate.convertAndSend(
                    "x.dead-letter",
                    appProperties.getQueue().getDlq(),
                    dlqMessage
            );

            log.warn("Message sent to DLQ: {} - Reason: {}",
                    message.getNotificationId(), errorReason);
        } catch (Exception e) {
            log.error("Failed to send message to DLQ: {}", e.getMessage(), e);
        }
    }
}