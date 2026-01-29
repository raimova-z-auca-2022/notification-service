package kg.notifications.gateway.messaging;

import com.rabbitmq.client.Channel;
import kg.notifications.gateway.dto.StatusEventDto;
import kg.notifications.gateway.service.NotificationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StatusEventsListener {

    private final NotificationStatusService statusService;

    @RabbitListener(queues = "${app.rabbit.queueStatusGateway}",
            containerFactory = "rabbitListenerContainerFactory")
    public void onStatus(StatusEventDto evt,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                         @Header(name = AmqpHeaders.CORRELATION_ID, required = false
                         ) String correlationId) throws IOException {
        try {
            statusService.handleStatusEvent(evt);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, false);
        }
    }
}
