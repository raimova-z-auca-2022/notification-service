package kg.notifications.gateway.messaging;

import com.rabbitmq.client.Channel;
import kg.notifications.gateway.dto.StatusEventDto;
import kg.notifications.gateway.repository.NotificationEventRepository;
import kg.notifications.gateway.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StatusEventsListener {

    private final NotificationRepository notificationRepository;
    private final NotificationEventRepository eventRepository;

    @RabbitListener(queues = "${app.rabbit.queueStatusGateway}", containerFactory = "rabbitListenerContainerFactory")
    public void onStatus(StatusEventDto evt,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            UUID id = UUID.fromString(evt.notificationId());
            notificationRepository.updateFromStatusEvent(
                    id,
                    evt.status(),
                    evt.attempt(),
                    evt.errorCode(),
                    evt.errorMessage(),
                    evt.providerMessageId()
            );
            eventRepository.insertEvent(
                    id,
                    "STATUS_CHANGED",
                    evt.status().name(),
                    evt.attempt(),
                    evt.errorCode(),
                    evt.errorMessage(),
                    evt.providerMessageId()
            );
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, false);
        }
    }
}
