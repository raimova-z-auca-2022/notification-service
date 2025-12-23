package kg.notifications.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppProperties props;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                              Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    /**
     * Single source of truth: gateway declares all exchanges/queues/bindings.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);

        declareTopology(admin);
        return admin;
    }

    private void declareTopology(RabbitAdmin admin) {
        AppProperties.Rabbit r = props.getRabbit();

        DirectExchange notificationExchange = new DirectExchange(r.getExchangeNotification(), true, false);
        TopicExchange statusExchange = new TopicExchange(r.getExchangeStatus(), true, false);
        admin.declareExchange(notificationExchange);
        admin.declareExchange(statusExchange);

        // Main queues
        Queue emailQueue = new Queue(r.getQueueEmail(), true);
        Queue telegramQueue = new Queue(r.getQueueTelegram(), true);
        admin.declareQueue(emailQueue);
        admin.declareQueue(telegramQueue);

        admin.declareBinding(BindingBuilder.bind(emailQueue).to(notificationExchange).with(r.getRoutingEmail()));
        admin.declareBinding(BindingBuilder.bind(telegramQueue).to(notificationExchange).with(r.getRoutingTelegram()));

        // Status queue (gateway consumes)
        Queue statusQueue = new Queue(r.getQueueStatusGateway(), true);
        admin.declareQueue(statusQueue);
        admin.declareBinding(BindingBuilder.bind(statusQueue).to(statusExchange).with("status.*"));

        // Retry queues (TTL + DLX back to x.notification)
        declareRetryAndDlq(admin, notificationExchange.getName(), r.getRoutingEmail(), r.getEmailRetryQueues(), r.getEmailDlq());
        declareRetryAndDlq(admin, notificationExchange.getName(), r.getRoutingTelegram(), r.getTelegramRetryQueues(), r.getTelegramDlq());
    }

    private void declareRetryAndDlq(RabbitAdmin admin,
                                   String deadLetterExchangeName,
                                   String deadLetterRoutingKey,
                                   List<String> retryQueueNames,
                                   String dlqName) {
        // DLQ
        if (dlqName != null && !dlqName.isBlank()) {
            admin.declareQueue(new Queue(dlqName, true));
        }

        // Map retry queue name to TTL (based on suffix)
        for (String qName : retryQueueNames) {
            if (qName == null || qName.isBlank()) {
                continue;
            }
            long ttlMs = inferTtlMs(qName);
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", ttlMs);
            args.put("x-dead-letter-exchange", deadLetterExchangeName);
            args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
            Queue retryQueue = new Queue(qName, true, false, false, args);
            admin.declareQueue(retryQueue);
        }
    }

    private long inferTtlMs(String qName) {
        // q.notification.email.retry.15s / 60s / 300s / 900s / 3600s
        String[] parts = qName.split("\\.");
        String last = parts[parts.length - 1];
        if (last.endsWith("s")) {
            String num = last.substring(0, last.length() - 1);
            try {
                long seconds = Long.parseLong(num);
                return seconds * 1000L;
            } catch (NumberFormatException ignored) {
            }
        }
        return 15000L;
    }
}
