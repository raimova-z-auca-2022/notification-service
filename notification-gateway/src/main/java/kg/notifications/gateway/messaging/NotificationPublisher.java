package kg.notifications.gateway.messaging;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.dto.NotificationCommandDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties props;

    public boolean publish(NotificationCommandDto dto) {
        String routingKey = resolveRoutingKey(dto.type());
        CorrelationData correlation = new CorrelationData(dto.notificationId());

        rabbitTemplate.convertAndSend(
                props.getRabbit().getExchangeNotification(),
                routingKey,
                dto,
                message -> {
                    message.getMessageProperties().setContentType("application/json");
                    message.getMessageProperties().setCorrelationId(dto.notificationId());
                    return message;
                },
                correlation
        );

        try {
            CorrelationData.Confirm confirm = correlation.getFuture()
                    .get(props.getPublish().getConfirmTimeoutMs(), TimeUnit.MILLISECONDS);
            if (confirm == null) {
                log.warn("Publish confirm is null for notificationId={}", dto.notificationId());
                return false;
            }
            if (!confirm.isAck()) {
                log.warn("Publish was NACKed for notificationId={} confirm={}", dto.notificationId(), confirm);
                return false;
            }
            log.debug("Publish ACK received for notificationId={}", dto.notificationId());
            return true;
        } catch (Exception e) {
            log.error("Exception while waiting for publish confirm for notificationId={}: {}", dto.notificationId(), e.getMessage(), e);
            return false;
        }
    }

    private String resolveRoutingKey(NotificationType type) {
        return switch (type) {
            case EMAIL -> props.getRabbit().getRoutingEmail();
            case TELEGRAM -> props.getRabbit().getRoutingTelegram();
            case WHATSAPP -> {
                String configured = props.getRabbit().getRoutingSms();
                if (configured != null && !configured.isBlank()) yield configured;
                yield "sms";
            }
        };
    }
}