package kg.notifications.telegram.service.impl;

import kg.notifications.telegram.config.AppProperties;
import kg.notifications.telegram.service.TelegramDlqService;
import kg.notifications.telegram.service.TelegramRetryService;
import kg.notifications.telegram.dto.NotificationCommandDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramRetryServiceImpl implements TelegramRetryService<NotificationCommandDto> {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;
    private final TelegramDlqService dlqService; // Внедряем наш новый сервис

    private static final String HEADER_X_RETRIES_COUNT = "x-retries-count";
    private static final String HEADER_X_EXCEPTION = "x-exception-message";

    @Override
    public void handleError(NotificationCommandDto dto, Integer currentRetryCount, Throwable ex) {
        int attempt = (currentRetryCount == null) ? 0 : currentRetryCount;
        int nextAttempt = attempt + 1;

        List<String> retryQueues = appProperties.getRabbit().getTelegramRetryQueues();


        if (retryQueues != null && nextAttempt <= retryQueues.size()) {
            String targetQueue = retryQueues.get(nextAttempt - 1);

            log.warn("Retry #{} for telegram. Queue: {}. Error: {}", nextAttempt, targetQueue, ex.getMessage());

            sendToRetryQueue(dto, targetQueue, nextAttempt, ex.getMessage());
        } else {
            log.error("Retries exhausted for telegram. Sending to DLQ.");

            dlqService.sendToDlq(
                    dto,
                    appProperties.getRabbit().getTelegramDlq(),
                    appProperties.getRabbit().getQueueTelegram(),
                    ex.getMessage()
            );
        }
    }

    private void sendToRetryQueue(NotificationCommandDto dto, String queue, int retryCount, String error) {
        rabbitTemplate.convertAndSend(queue, dto, m -> {
            m.getMessageProperties().setHeader(HEADER_X_RETRIES_COUNT, retryCount);
            m.getMessageProperties().setHeader(HEADER_X_EXCEPTION, error);
            return m;
        });
    }
}