package kg.notifications.telegram.messaging;

import kg.notifications.telegram.dto.NotificationCommandDto;
import kg.notifications.telegram.dto.RegistrationCommandDto;
import kg.notifications.telegram.service.TelegramRetryService;
import kg.notifications.telegram.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramEventListener {

    private final TelegramService telegramService;
    private final TelegramRetryService notificationRetryService;


    @RabbitListener(queues = "${app.rabbit.queueTelegramRegistration}")
    public void handleRegistration(RegistrationCommandDto cmd) {
        log.info("Received registration link request: {}", cmd.linkCode());
        telegramService.handleRegistration(cmd);
    }


    @RabbitListener(queues = "${app.rabbit.queueTelegram}")
    public void handleNotification(NotificationCommandDto notificationDto,
                                   @Header(required = false, name = "x-retries-count") Integer retryCount,
                                   @Header(name = AmqpHeaders.CORRELATION_ID, required = false) String correlationId) {
        log.info("Received notification for recipient: {}. Retry count: {}", notificationDto.recipient(), retryCount, correlationId);

        try {
            telegramService.sendNotification(notificationDto);
        } catch (Exception e) {
            log.error("Failed to send telegram notification: {}", e.getMessage());
            notificationRetryService.handleError(notificationDto, retryCount, e);
        }
    }
}