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

    // ---------- Common ----------

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf,
            Jackson2JsonMessageConverter converter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(converter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    // ---------- RabbitAdmin (AUTO, SAFE) ----------

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory cf) {
        RabbitAdmin admin = new RabbitAdmin(cf);
        admin.setAutoStartup(true);
        admin.setIgnoreDeclarationExceptions(true);
        return admin;
    }

    // ---------- Exchanges ----------

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(
                props.getRabbit().getExchangeNotification(),
                true,
                false
        );
    }

    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(
                props.getRabbit().getExchangeStatus(),
                true,
                false
        );
    }

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(
                props.getRabbit().getExchangeDelayed(),
                "x-delayed-message",
                true,
                false,
                args
        );
    }

    // ---------- Main Queues ----------

    @Bean
    public Queue emailQueue() {
        return new Queue(props.getRabbit().getQueueEmail(), true);
    }

    @Bean
    public Queue telegramQueue() {
        return new Queue(props.getRabbit().getQueueTelegram(), true);
    }

    @Bean
    public Queue whatsappQueue() {
        return new Queue(props.getRabbit().getQueueWhatsapp(), true);
    }

    @Bean
    public Queue telegramRegistrationQueue() {
        return new Queue(props.getRabbit().getQueueTelegramRegistration(), true);
    }

    @Bean
    public Queue statusQueueGateway() {
        return new Queue(props.getRabbit().getQueueStatusGateway(), true);
    }


    @Bean
    public Queue scheduledMessagesQueue() {
        return new Queue(props.getRabbit().getQueueScheduledMessages(), true);
    }


    // ---------- Bindings ----------

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(notificationExchange())
                .with(props.getRabbit().getRoutingEmail());
    }

    @Bean
    public Binding telegramBinding() {
        return BindingBuilder.bind(telegramQueue())
                .to(notificationExchange())
                .with(props.getRabbit().getRoutingTelegram());
    }

    @Bean
    public Binding whatsappBinding() {
        return BindingBuilder.bind(whatsappQueue())
                .to(notificationExchange())
                .with(props.getRabbit().getRoutingWhatsapp());
    }

    @Bean
    public Binding telegramRegistrationBinding() {
        return BindingBuilder.bind(telegramRegistrationQueue())
                .to(notificationExchange())
                .with(props.getRabbit().getRoutingTelegramRegistration());
    }

    @Bean
    public Binding statusBinding() {
        return BindingBuilder.bind(statusQueueGateway())
                .to(statusExchange())
                .with("status.*");
    }


    @Bean
    public Binding scheduledMessagesBinding() {
        return BindingBuilder.bind(scheduledMessagesQueue())
                .to(delayedExchange())
                .with(props.getRabbit().getRoutingScheduled())
                .noargs();
    }

    // ---------- Retry + DLQ ----------

    @Bean
    public Declarables emailRetryTopology() {
        return retryTopology(
                props.getRabbit().getEmailRetryQueues(),
                props.getRabbit().getEmailDlq(),
                props.getRabbit().getRoutingEmail()
        );
    }

    @Bean
    public Declarables telegramRetryTopology() {
        return retryTopology(
                props.getRabbit().getTelegramRetryQueues(),
                props.getRabbit().getTelegramDlq(),
                props.getRabbit().getRoutingTelegram()
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
            args.put("x-dead-letter-exchange", props.getRabbit().getExchangeNotification());
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