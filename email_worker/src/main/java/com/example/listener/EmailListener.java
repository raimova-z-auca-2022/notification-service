package com.example.listener;

import com.example.dto.NotificationJobMessage;
import com.example.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.entity.Notification;
import com.example.enums.NotificationStatus;
import com.example.repository.NotificationJdbcRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import com.rabbitmq.client.Channel;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private static final int MAX_RETRY = 3;

    private final NotificationJdbcRepository repository;
    private final EmailService emailService;

    @RabbitListener(
            queues = "ns.email.q",
            concurrency = "3-5"
    )
    public void handle(NotificationJobMessage message,
                       Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        Notification notification = repository.findById(message.getNotificationId())
                .orElseThrow();

        try {
            emailService.sendEmail(notification);

            repository.updateStatus(
                    notification.getNotificationId(),
                    NotificationStatus.SENT,
                    null,
                    LocalDateTime.now()
            );

            channel.basicAck(tag, false);
            log.info("Email sent {}", notification.getNotificationId());

        } catch (Exception ex) {

            int retry = notification.getRetryCount() + 1;

            if (retry >= MAX_RETRY) {
                repository.updateStatus(
                        notification.getNotificationId(),
                        NotificationStatus.FAILED,
                        ex.getMessage(),
                        null
                );

                channel.basicReject(tag, false); // → DLQ
                log.error("Moved to DLQ {}", notification.getNotificationId());
            } else {
                repository.incrementRetry(
                        notification.getNotificationId(),
                        ex.getMessage()
                );
                channel.basicNack(tag, false, true); // retry
                log.warn("Retry {} for {}", retry, notification.getNotificationId());
            }
        }
    }
}
