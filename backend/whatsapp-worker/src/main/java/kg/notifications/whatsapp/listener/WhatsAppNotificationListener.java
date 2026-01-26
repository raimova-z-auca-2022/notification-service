package kg.notifications.whatsapp.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.messaging.dto.StatusEventDto;
import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
import kg.notifications.whatsapp.service.DLQService;
import kg.notifications.whatsapp.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotificationListener {

    private final ObjectMapper objectMapper;
    private final WhatsAppService whatsAppService;
    private final DLQService dlqService;
    private final RabbitTemplate rabbitTemplate; // Добавили для отправки статуса

    @RabbitListener(queues = "${app.whatsapp.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message amqpMessage, Channel channel) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        String payload = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);

        try {
            String json = payload;
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = objectMapper.readValue(json, String.class);
            }

            WhatsAppNotificationMessage message = objectMapper.readValue(json, WhatsAppNotificationMessage.class);
            log.info("Processing WhatsApp notification [ID: {}, Recipient: {}]", message.getNotificationId(), message.getRecipient());

            // 1. Отправляем сообщение
            whatsAppService.sendMessage(message);

            // 2. Отправляем статус SENT обратно в гейтвей
            sendStatusUpdate(message.getNotificationId().toString(), NotificationStatus.SENT, null, null);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());

            // Если ошибка — отправляем статус FAILED
            try {
                var node = objectMapper.readTree(payload);
                String id = node.has("notificationId") ? node.get("notificationId").asText() : UUID.randomUUID().toString();
                sendStatusUpdate(id, NotificationStatus.FAILED, "ERR_500", e.getMessage());
            } catch (Exception ignored) {}

            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendStatusUpdate(String id, NotificationStatus status, String errCode, String errMsg) {
        StatusEventDto event = new StatusEventDto(
                id,
                NotificationType.WHATSAPP,
                status,
                1,
                errCode,
                errMsg,
                null,
                OffsetDateTime.now()
        );
        // Отправляем в exchange x.status с ключом status.updates
        rabbitTemplate.convertAndSend("x.status", "status.updates", event);
        log.info("Status update sent to Gateway: {} for ID: {}", status, id);
    }
}