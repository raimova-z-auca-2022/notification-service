package kg.notifications.telegram.listener;

import kg.notifications.telegram.dto.NotificationDto;
import kg.notifications.telegram.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramNotificationListener {

    private final TelegramService telegramService;


    @RabbitListener(queues = "${rabbitmq.queues.telegram}")
    public void receiveMessage(NotificationDto notificationDto) {
        log.info("Received message from RabbitMQ: {}", notificationDto);


        telegramService.sendNotification(notificationDto);
    }
}