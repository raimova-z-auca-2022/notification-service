package kg.notifications.whatsapp.service.impl;

import kg.notifications.whatsapp.config.AppProperties;
import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
import kg.notifications.whatsapp.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetryServiceImpl implements RetryService {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;

    @Override
    public void scheduleRetry(Message originalMessage,
                              WhatsAppNotificationMessage notificationMessage,
                              int nextRetryCount) {

        long delayMillis = calculateDelayMillis(nextRetryCount);

        Map<String, Object> headers = new HashMap<>();
        headers.putAll(originalMessage.getMessageProperties().getHeaders());
        headers.put("x-retry-count", nextRetryCount);
        headers.put("x-scheduled-time", new Date());

        Message retryMessage = MessageBuilder
                .withBody(originalMessage.getBody())
                .copyHeaders(headers)
                .setHeader("x-delay", delayMillis)
                .build();

        rabbitTemplate.send(
                "x.notification",
                "whatsapp",
                retryMessage
        );

        log.debug("Scheduled retry #{} in {}ms for notification {}",
                nextRetryCount, delayMillis, notificationMessage.getNotificationId());
    }

    private long calculateDelayMillis(int retryCount) {
        List<String> delays = appProperties.getRetry().getDelays();

        if (delays.isEmpty()) {
            return switch (retryCount) {
                case 1 -> 60_000L;
                case 2 -> 300_000L;
                case 3 -> 900_000L;
                case 4 -> 3_600_000L;
                default -> 14_400_000L;
            };
        } else {
            String delayStr = delays.get(Math.min(retryCount - 1, delays.size() - 1));
            return parseDelayToMillis(delayStr);
        }
    }

    private long parseDelayToMillis(String delay) {
        if (delay.endsWith("s")) {
            return Long.parseLong(delay.substring(0, delay.length() - 1)) * 1000;
        } else if (delay.endsWith("m")) {
            return Long.parseLong(delay.substring(0, delay.length() - 1)) * 60_000;
        } else if (delay.endsWith("h")) {
            return Long.parseLong(delay.substring(0, delay.length() - 1)) * 3_600_000;
        }
        return Long.parseLong(delay) * 1000;
    }
}