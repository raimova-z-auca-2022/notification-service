package kg.notifications.gateway.messaging;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.dto.NotificationCommandDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties props;

    public boolean publish(NotificationCommandDto dto) {
        String routingKey = resolveRoutingKey(dto.type());
        CorrelationData correlationData = new CorrelationData(dto.notificationId());

        rabbitTemplate.convertAndSend(
                props.getRabbit().getExchangeNotification(),
                routingKey,
                dto,
                message -> {
                    message.getMessageProperties().setContentType("application/json");
                    message.getMessageProperties().setCorrelationId(dto.notificationId());
                    return message;
                },
                correlationData
        );

        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(props.getPublish().getConfirmTimeoutMs(), TimeUnit.MILLISECONDS);
            return confirm != null && confirm.isAck();
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveRoutingKey(NotificationType type) {
        return switch (type) {
            case EMAIL -> props.getRabbit().getRoutingEmail();
            case TELEGRAM -> props.getRabbit().getRoutingTelegram();
            case WHATSAPP -> "whatsapp";
        };
    }
}
