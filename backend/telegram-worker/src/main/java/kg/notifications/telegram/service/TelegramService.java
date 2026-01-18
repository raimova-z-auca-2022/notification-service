package kg.notifications.telegram.service;

import kg.notifications.telegram.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramService {

    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.api-url}")
    private String apiUrl;

    public void sendNotification(NotificationDto dto) {
        log.info("Processing notification ID: {}, sending to chat: {}", dto.notificationId(), dto.recipient());

        String url = String.format("%s/bot%s/sendMessage", apiUrl, botToken);

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", dto.recipient());
        body.put("text", dto.text());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Message sent. Response: {}", response.getBody());
            } else {
                log.error("Telegram error: {}", response.getStatusCode());
                throw new RuntimeException("Telegram API Error");
            }
        } catch (Exception e) {
            log.error("Failed to send message ID {}: {}", dto.notificationId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}