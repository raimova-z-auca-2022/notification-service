package kg.notifications.whatsapp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.notifications.whatsapp.dto.NotificationStatusEvent;
import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
import kg.notifications.whatsapp.service.NotificationStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationStatusServiceImpl implements NotificationStatusService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendProcessingStatus(WhatsAppNotificationMessage message) {
        NotificationStatusEvent event = NotificationStatusEvent.builder()
                .notificationId(message.getNotificationId())
                .channel("WHATSAPP")
                .status("PROCESSING")
                .timestamp(LocalDateTime.now())
                .metadata(Map.of("retryCount", message.getRetryCount()))
                .build();

        sendStatusEvent(event);
    }

    @Override
    public void sendSuccessStatus(WhatsAppNotificationMessage message, String messageId) {
        NotificationStatusEvent event = NotificationStatusEvent.builder()
                .notificationId(message.getNotificationId())
                .channel("WHATSAPP")
                .status("SENT")
                .messageId(messageId)
                .timestamp(LocalDateTime.now())
                .build();

        sendStatusEvent(event);
    }

    @Override
    public void sendErrorStatus(WhatsAppNotificationMessage message, String errorMessage) {
        NotificationStatusEvent event = NotificationStatusEvent.builder()
                .notificationId(message.getNotificationId())
                .channel("WHATSAPP")
                .status("FAILED")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .metadata(Map.of(
                        "retryCount", message.getRetryCount(),
                        "recipient", message.getMaskedRecipient()
                ))
                .build();

        sendStatusEvent(event);
    }

    private void sendStatusEvent(NotificationStatusEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    "x.status",
                    "notification.status",
                    event
            );
            log.debug("Status event sent: {}", event.getStatus());
        } catch (Exception e) {
            log.error("Failed to send status event: {}", e.getMessage(), e);
        }
    }
}