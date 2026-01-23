package kg.notifications.email.listener;

import kg.notifications.email.dto.NotificationCommandDto;
import kg.notifications.email.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbit.queueEmail}")
    public void handle(NotificationCommandDto cmd) {
        log.info("Received email notification {}", cmd.notificationId());
        System.out.println("----------------------------**********************************");
        notificationService.processEmailNotification(cmd);
    }
}
