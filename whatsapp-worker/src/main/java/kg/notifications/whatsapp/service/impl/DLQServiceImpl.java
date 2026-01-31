package kg.notifications.whatsapp.service.impl;

import kg.notifications.whatsapp.service.DLQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DLQServiceImpl implements DLQService {

    private final RabbitTemplate rabbitTemplate;

    private static final String HEADER_X_EXCEPTION = "x-exception-message";
    private static final String HEADER_X_ORIGINAL_QUEUE = "x-original-queue";
    private static final String HEADER_X_FAILED_AT = "x-failed-at";

    @Override
    public void sendToDlq(Object payload, String dlqQueueName, String originalQueue, String exceptionMsg) {
        log.error("Moving message to DLQ: {}. Original Queue: {}. Error: {}",
                dlqQueueName, originalQueue, exceptionMsg);

        try {
            rabbitTemplate.convertAndSend(dlqQueueName, payload, message -> {
                message.getMessageProperties().setHeader(HEADER_X_EXCEPTION, exceptionMsg);
                message.getMessageProperties().setHeader(HEADER_X_ORIGINAL_QUEUE, originalQueue);
                message.getMessageProperties().setHeader(HEADER_X_FAILED_AT, java.time.Instant.now().toString());
                return message;
            });
        } catch (Exception e) {
            log.error("CRITICAL: Failed to send message to DLQ! Payload: {}", payload, e);
        }
    }
}