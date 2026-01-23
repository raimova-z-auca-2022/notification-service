package kg.notifications.telegram.listener;

import kg.notifications.telegram.dto.NotificationCommandDto;
import kg.notifications.telegram.dto.RegistrationCommandDto;
import kg.notifications.telegram.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramEventListener {

    private final TelegramService telegramService;


    @RabbitListener(queues = "${app.rabbit.queueTelegramRegistration}")
    public void handleRegistration(RegistrationCommandDto cmd) {
        log.info("Received registration link request: {}", cmd.linkCode());
        telegramService.handleRegistration(cmd);
    }


    @RabbitListener(queues = "${app.rabbit.queueTelegram}")
    public void handleNotification(NotificationCommandDto notificationDto) {
        log.info("Received notification for recipient phone: {}", notificationDto.recipient());

        telegramService.sendNotification(notificationDto);
    }
}