package kg.notifications.email.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppProperties appProperties;

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        // Это позволяет воркеру самому определять тип DTO на основе метода слушателя
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(appProperties.getRabbit().getExchangeNotification(), true, false);
    }

    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(
                appProperties.getRabbit().getExchangeStatus(),
                true,
                false
        );
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(appProperties.getRabbit().getQueueEmail(), true);
    }

    @Bean
    public Queue statusQueueGateway() {
        return new Queue(appProperties.getRabbit().getQueueStatusGateway(), true);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(notificationExchange)
                .with(appProperties.getRabbit().getRoutingEmail());
    }

    @Bean
    public Declarables emailRetryTopology() {
        return retryTopology(
                appProperties.getRabbit().getEmailRetryQueues(),
                appProperties.getRabbit().getEmailDlq(),
                appProperties.getRabbit().getRoutingEmail()
        );
    }

    private Declarables retryTopology(
            List<String> retryQueues,
            String dlq,
            String routingKey
    ) {
        Map<String, Object> dlqArgs = new HashMap<>();
        Queue dlqQueue = new Queue(dlq, true, false, false, dlqArgs);

        List<Declarable> declarables = new java.util.ArrayList<>();
        declarables.add(dlqQueue);

        for (String q : retryQueues) {
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", inferTtlMs(q));
            args.put("x-dead-letter-exchange", appProperties.getRabbit().getExchangeNotification());
            args.put("x-dead-letter-routing-key", routingKey);

            declarables.add(new Queue(q, true, false, false, args));
        }

        return new Declarables(declarables);
    }

    private long inferTtlMs(String qName) {
        String last = qName.substring(qName.lastIndexOf('.') + 1);
        if (last.endsWith("s")) {
            try {
                return Long.parseLong(last.replace("s", "")) * 1000L;
            } catch (Exception ignored) {}
        }
        return 15000L;
    }
}