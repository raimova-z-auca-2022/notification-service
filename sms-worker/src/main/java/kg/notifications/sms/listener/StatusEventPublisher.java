package kg.notifications.sms.listener;

import kg.notifications.sms.config.AppProperties;
import kg.notifications.sms.dto.*;
import kg.notifications.sms.dto.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class StatusEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;

    public void publishProcessing(NotificationCommandDto cmd) {
        publish(cmd, NotificationStatus.PROCESSING, null, null, null);
    }

    public void publishSent(NotificationCommandDto cmd, String providerMessageId) {
        publish(cmd, NotificationStatus.SENT, null, null, providerMessageId);
    }

    public void publishFailed(
            NotificationCommandDto cmd,
            String errorCode,
            String errorMessage
    ) {
        publish(cmd, NotificationStatus.FAILED, errorCode, errorMessage, null);
    }

    private void publish(
            NotificationCommandDto cmd,
            NotificationStatus status,
            String errorCode,
            String errorMessage,
            String providerMessageId
    ) {
        StatusEventDto event = new StatusEventDto(
                cmd.notificationId(),
                NotificationType.WHATSAPP,
                status,
                cmd.attempt(),
                errorCode,
                errorMessage,
                providerMessageId,
                OffsetDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                appProperties.getRabbit().getExchangeStatus(),
                "status.sms",
                event,
                message -> {
                    message.getMessageProperties().setCorrelationId(cmd.notificationId());
                    return message;
                }
        );
    }
}
