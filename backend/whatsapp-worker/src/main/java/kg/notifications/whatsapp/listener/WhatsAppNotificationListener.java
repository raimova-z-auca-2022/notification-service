package kg.notifications.whatsapp.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import kg.notifications.gateway.messaging.dto.StatusEventDto;
import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
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
    private final RabbitTemplate rabbitTemplate;

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
            log.info("Processing WhatsApp notification [ID: {}]", message.getNotificationId());

            whatsAppService.sendMessage(message);

            // Отправляем статус текстом
            sendStatusUpdate(message.getNotificationId().toString(), "SENT", null, null);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
            try {
                var node = objectMapper.readTree(payload);
                String id = node.has("notificationId") ? node.get("notificationId").asText() : UUID.randomUUID().toString();
                sendStatusUpdate(id, "FAILED", "ERR_500", e.getMessage());
            } catch (Exception ignored) {}
            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendStatusUpdate(String id, String status, String errCode, String errMsg) {
        StatusEventDto event = StatusEventDto.builder()
                .notificationId(id)
                .type("WHATSAPP")
                .status(status)
                .attempt(1)
                .errorCode(errCode)
                .errorMessage(errMsg)
                .occurredAt(OffsetDateTime.now())
                .build();

        rabbitTemplate.convertAndSend("x.status", "status.updates", event);
        log.info("Status update sent to Gateway: {} for ID: {}", status, id);
    }
}