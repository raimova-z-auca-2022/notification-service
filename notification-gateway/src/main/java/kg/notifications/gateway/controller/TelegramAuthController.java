package kg.notifications.gateway.controller;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.RegistrationCommandDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramAuthController {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;

    @PostMapping("/generate-link")
    public ResponseEntity<String> generateLink(@RequestParam(required = false) String userId) {

        // 1. Генерируем уникальный токен
        String linkCode = UUID.randomUUID().toString();

        // 2. Создаем событие для worker
        RegistrationCommandDto command = new RegistrationCommandDto(
                linkCode,
                userId != null ? userId : "anonymous",
                LocalDateTime.now().plusHours(1)
        );

        // 3. Отправляем в очередь для регистрации Telegram
        rabbitTemplate.convertAndSend(
                appProperties.getRabbit().getExchangeNotification(),         // x.notification
                appProperties.getRabbit().getRoutingTelegramRegistration(), // telegram.registration
                command
        );

        System.out.println(" [x] Sent to RabbitMQ: -----------------------------------************************ ");

        // 4. Формируем ссылку
        String botName = appProperties.getTelegram().getBotName();
        String telegramLink = "https://t.me/" + botName + "?start=" + linkCode;

        return ResponseEntity.ok(telegramLink);
    }
}
