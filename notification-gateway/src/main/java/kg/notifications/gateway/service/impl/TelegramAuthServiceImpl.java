package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.RegistrationCommandDto;
import kg.notifications.gateway.service.TelegramAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthServiceImpl implements TelegramAuthService {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;

    @Override
    public String generateRegistrationLink(String userId) {

        String linkCode = UUID.randomUUID().toString();
        String internalUserId = userId != null ? userId : "anonymous";

        RegistrationCommandDto command = new RegistrationCommandDto(
                linkCode,
                internalUserId,
                LocalDateTime.now().plusHours(1)
        );

        rabbitTemplate.convertAndSend(
                appProperties.getRabbit().getExchangeNotification(),
                appProperties.getRabbit().getRoutingTelegramRegistration(),
                command
        );

        String telegramLink = "https://t.me/"
                + appProperties.getTelegram().getBotName()
                + "?start="
                + linkCode;

        log.info("Generated telegram registration link for user {}", internalUserId);

        return telegramLink;
    }
}
