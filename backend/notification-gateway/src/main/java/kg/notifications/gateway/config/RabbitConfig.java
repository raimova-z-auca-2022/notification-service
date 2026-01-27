package kg.notifications.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper; // ДОБАВЛЕНО
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper; // ДОБАВЛЕНО
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
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        // Игнорируем метаданные классов, чтобы воркер мог десериализовать в свой пакет
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
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

        // Queues
        Queue emailQueue = new Queue(r.getQueueEmail(), true);
        Queue telegramQueue = new Queue(r.getQueueTelegram(), true);
        Queue whatsappQueue = new Queue("q.notification.whatsapp", true); // Исправлено здесь

        admin.declareQueue(emailQueue);
        admin.declareQueue(telegramQueue);
        admin.declareQueue(whatsappQueue);

        admin.declareBinding(BindingBuilder.bind(emailQueue).to(notificationExchange).with(r.getRoutingEmail()));
        admin.declareBinding(BindingBuilder.bind(telegramQueue).to(notificationExchange).with(r.getRoutingTelegram()));
        admin.declareBinding(BindingBuilder.bind(whatsappQueue).to(notificationExchange).with(r.getRoutingWhatsapp()));

// СТАЛО (Жестко задаем "q.status", чтобы слушатель точно нашел её)
        Queue statusQueue = new Queue("q.status", true); // <--- Важное изменение
        admin.declareQueue(statusQueue);
// Биндинг делаем на "status.#", чтобы ловить любые ключи (status.updates, status.error и т.д.)
        admin.declareBinding(BindingBuilder.bind(statusQueue).to(statusExchange).with("status.#"));

        declareRetryAndDlq(admin, notificationExchange.getName(), r.getRoutingEmail(), r.getEmailRetryQueues(), r.getEmailDlq());
        declareRetryAndDlq(admin, notificationExchange.getName(), r.getRoutingTelegram(), r.getTelegramRetryQueues(), r.getTelegramDlq());
    }

    private void declareRetryAndDlq(RabbitAdmin admin, String deadLetterExchangeName, String deadLetterRoutingKey, List<String> retryQueueNames, String dlqName) {
        if (dlqName != null && !dlqName.isBlank()) {
            admin.declareQueue(new Queue(dlqName, true));
        }
        for (String qName : retryQueueNames) {
            if (qName == null || qName.isBlank()) continue;
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", inferTtlMs(qName));
            args.put("x-dead-letter-exchange", deadLetterExchangeName);
            args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
            admin.declareQueue(new Queue(qName, true, false, false, args));
        }
    }

    private long inferTtlMs(String qName) {
        try {
            String[] parts = qName.split("\\.");
            String last = parts[parts.length - 1];
            if (last.endsWith("s")) return Long.parseLong(last.substring(0, last.length() - 1)) * 1000L;
        } catch (Exception ignored) {}
        return 15000L;
    }
}