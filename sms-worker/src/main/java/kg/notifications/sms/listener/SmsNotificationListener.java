package kg.notifications.sms.listener;

import kg.notifications.sms.config.AppProperties;
import kg.notifications.sms.dto.NotificationCommandDto;
import kg.notifications.sms.dto.NotificationStatus;
import kg.notifications.sms.dto.NotificationType;
import kg.notifications.sms.dto.StatusEventDto;
import kg.notifications.sms.service.RetryService;
import kg.notifications.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationListener {

    private final RabbitTemplate rabbitTemplate;
    private final SmsService smsService;
    private final RetryService retryService;
    private final AppProperties appProperties;

    @RabbitListener(queues = "${app.rabbit.queueSms}")
    public void handleNotification(NotificationCommandDto dto,
                                   @Header(required = false, name = "x-retries-count") Integer retryCount,
                                   @Header(name = AmqpHeaders.CORRELATION_ID, required = false) String correlationId) {
        log.info("Received SMS notification for recipient: {}. Retry count: {}",
                dto.recipient(),
                retryCount,
                correlationId);

        try {
            smsService.sendMessage(dto);
            sendStatusUpdate(dto.notificationId().toString(), "SENT", null, null);
        } catch (Exception e) {
            log.error("Failed to send SMS notification: {}", e.getMessage());
            retryService.handleError(dto, retryCount, e);
            sendStatusUpdate(dto.notificationId().toString(), "PROCESSING", null, null);
        }
    }

    private void sendStatusUpdate(String id, String status, String errCode, String errMsg) {
        StatusEventDto event = new StatusEventDto(
                id,
                NotificationType.SMS,
                NotificationStatus.valueOf(status),
                1,
                errCode,
                errMsg,
                null,
                OffsetDateTime.now()
        );


        rabbitTemplate.convertAndSend(
                appProperties.getRabbit().getExchangeStatus(),
                "status.sms",
                event);
        log.info("Status update sent to Gateway: {} for ID: {}", status, id);
    }
}