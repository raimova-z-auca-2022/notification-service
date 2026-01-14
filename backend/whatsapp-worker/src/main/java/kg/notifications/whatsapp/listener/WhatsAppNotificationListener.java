package kg.notifications.whatsapp.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import kg.notifications.whatsapp.config.AppProperties;
import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
import kg.notifications.whatsapp.exception.InvalidMessageException;
import kg.notifications.whatsapp.exception.WhatsAppApiException;
import kg.notifications.whatsapp.service.DLQService;
import kg.notifications.whatsapp.service.NotificationStatusService;
import kg.notifications.whatsapp.service.RetryService;
import kg.notifications.whatsapp.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppNotificationListener {

    private final WhatsAppService whatsAppService;
    private final RetryService retryService;
    private final DLQService dlqService;
    private final NotificationStatusService statusService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @RabbitListener(
            queues = "${app.whatsapp.queue.name}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void processNotification(
            Message amqpMessage,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(value = "x-retry-count", required = false, defaultValue = "0") int retryCount
    ) throws IOException {

        WhatsAppNotificationMessage message = null;

        try {
            message = objectMapper.readValue(amqpMessage.getBody(), WhatsAppNotificationMessage.class);

            if (message == null) {
                throw new IllegalArgumentException("Message body is null");
            }

            message.setRetryCount(retryCount);

            log.info("Processing WhatsApp notification [ID: {}, Retry: {}]",
                    message.getNotificationId(), retryCount);

            statusService.sendProcessingStatus(message);

            whatsAppService.sendMessage(message);

            channel.basicAck(deliveryTag, false);

            log.info("Successfully processed notification: {}",
                    message.getNotificationId());

        } catch (IllegalArgumentException | InvalidMessageException e) {
            log.error("Invalid message format: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);

            if (message != null) {
                dlqService.sendToDLQ(message, "INVALID_FORMAT: " + e.getMessage());
                statusService.sendErrorStatus(message, e.getMessage());
            }

        } catch (WhatsAppApiException e) {
            log.error("WhatsApp API error: {}", e.getMessage());

            if (e.isRetryable() && retryCount < appProperties.getApi().getMaxRetries()) {
                channel.basicNack(deliveryTag, false, false);
                retryService.scheduleRetry(amqpMessage, message, retryCount + 1);
                statusService.sendErrorStatus(message, "Retry scheduled: " + e.getMessage());

                log.info("Scheduled retry {}/{} for notification {}",
                        retryCount + 1, appProperties.getApi().getMaxRetries(),
                        message.getNotificationId());
            } else {
                channel.basicNack(deliveryTag, false, false);
                dlqService.sendToDLQ(message,
                        String.format("API_ERROR: %s (retries: %d)",
                                e.getMessage(), retryCount));
                statusService.sendErrorStatus(message, e.getMessage());

                log.warn("Sent to DLQ after {} retries: {}",
                        retryCount, message.getNotificationId());
            }

        } catch (Exception e) {
            log.error("Unexpected error processing message: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);

            if (message != null) {
                dlqService.sendToDLQ(message,
                        String.format("UNEXPECTED_ERROR: %s", e.getMessage()));
                statusService.sendErrorStatus(message, e.getMessage());
            }
        }
    }
}