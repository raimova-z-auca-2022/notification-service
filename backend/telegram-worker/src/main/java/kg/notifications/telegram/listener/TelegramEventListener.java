package kg.notifications.telegram.listener;

import kg.notifications.telegram.config.AppProperties;
import kg.notifications.telegram.dto.NotificationCommandDto;
import kg.notifications.telegram.dto.RegistrationCommandDto;
import kg.notifications.telegram.model.RegistrationRequest;
import kg.notifications.telegram.repository.RegistrationRequestRepository;
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
    private final RegistrationRequestRepository registrationRepository;

    /**
     * Очередь для создания ссылок регистрации.
     * Имя очереди берется из application.yaml -> app.rabbit.queueTelegramRegistration
     */
    @RabbitListener(queues = "${app.rabbit.queueTelegramRegistration}")
    public void handleRegistration(RegistrationCommandDto cmd) {
        log.info("Received registration link request: {}", cmd.linkCode());
        System.out.println(" [x] Get from RabbitMQ: -----------------------------------************************ ");

        RegistrationRequest request = new RegistrationRequest();
        request.setToken(cmd.linkCode());
        request.setInternalUserId(cmd.internalUserId());
        request.setExpiresAt(cmd.expiresAt());

        registrationRepository.save(request);
    }

    /**
     * Очередь для отправки уведомлений.
     * Имя очереди берется из application.yaml -> app.rabbit.queueTelegram
     */
    @RabbitListener(queues = "${app.rabbit.queueTelegram}")
    public void handleNotification(NotificationCommandDto notificationDto) {
        log.info("Received notification for recipient phone: {}", notificationDto.recipient());

        telegramService.sendNotification(notificationDto);
    }
}