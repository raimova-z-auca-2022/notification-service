package kg.notifications.email.service.impl;

import kg.notifications.email.service.DlqService;
import kg.notifications.email.service.EmailRetryService;
import kg.notifications.email.config.AppProperties;
import kg.notifications.email.dto.NotificationCommandDto; // Обратите внимание: пакет email
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRetryServiceImpl implements EmailRetryService<NotificationCommandDto> {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;
    private final DlqService dlqService;

    private static final String HEADER_X_RETRIES_COUNT = "x-retries-count";
    private static final String HEADER_X_EXCEPTION = "x-exception-message";

    @Override
    public void handleError(NotificationCommandDto dto, Integer currentRetryCount, Throwable ex) {
        int attempt = (currentRetryCount == null) ? 0 : currentRetryCount;
        int nextAttempt = attempt + 1;

        List<String> retryQueues = appProperties.getRabbit().getEmailRetryQueues();

        if (retryQueues != null && nextAttempt <= retryQueues.size()) {
            String targetQueue = retryQueues.get(nextAttempt - 1);

            log.warn("Email retry #{} triggered for ID {}. Sending to: {}. Error: {}",
                    nextAttempt, dto.notificationId(), targetQueue, ex.getMessage());

            sendToRetryQueue(dto, targetQueue, nextAttempt, ex.getMessage());
        } else {
            log.error("Email retries exhausted for ID {}. Moving to DLQ.", dto.notificationId());

            dlqService.sendToDlq(
                    dto,
                    appProperties.getRabbit().getEmailDlq(),
                    appProperties.getRabbit().getQueueEmail(),
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
