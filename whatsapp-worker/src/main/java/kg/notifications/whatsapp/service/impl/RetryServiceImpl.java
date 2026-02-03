package kg.notifications.whatsapp.service.impl;

import kg.notifications.whatsapp.config.AppProperties;
import kg.notifications.whatsapp.dto.NotificationCommandDto;
import kg.notifications.whatsapp.service.DLQService;
import kg.notifications.whatsapp.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RetryServiceImpl implements RetryService {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;
    private final DLQService dlqService; // Внедряем наш новый сервис

    private static final String HEADER_X_RETRIES_COUNT = "x-retries-count";
    private static final String HEADER_X_EXCEPTION = "x-exception-message";

    @Override
    public void handleError(NotificationCommandDto message, Integer currentRetryCount, Throwable ex) {
        if (message == null || ex == null) {
            log.warn("Retry handler called with null parameters");
            return;
        }
        
        int attempt = (currentRetryCount == null) ? 0 : currentRetryCount;
        int nextAttempt = attempt + 1;

        List<String> retryQueues = appProperties.getRabbit().getWhatsappRetryQueues();

        if (shouldRetry(nextAttempt, retryQueues)) {
            String targetQueue = retryQueues.get(nextAttempt - 1);
            log.warn("WhatsApp retry #{} for notificationId={}. Queue={}", nextAttempt, message.notificationId(), targetQueue);
            sendToRetryQueue(message, targetQueue, nextAttempt, ex.getMessage());
        } else {
            log.error("WhatsApp retries exhausted for notificationId={} after {} attempts", message.notificationId(), nextAttempt);
            dlqService.sendToDlq(
                    message,
                    appProperties.getRabbit().getWhatsappDlq(),
                    appProperties.getRabbit().getQueueWhatsapp(),
                    ex.getMessage()
            );
        }
    }
    
    private boolean shouldRetry(int nextAttempt, List<String> retryQueues) {
        return retryQueues != null && nextAttempt <= retryQueues.size();
    }

    private void sendToRetryQueue(NotificationCommandDto message, String queue, int retryCount, String error) {
        rabbitTemplate.convertAndSend(queue, message, m -> {
            m.getMessageProperties().setHeader(HEADER_X_RETRIES_COUNT, retryCount);
            m.getMessageProperties().setHeader(HEADER_X_EXCEPTION, error);
            return m;
        });
    }
}