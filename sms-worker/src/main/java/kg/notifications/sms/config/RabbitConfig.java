package kg.notifications.sms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppProperties appProperties;

    // --- QUEUES ---

    @Bean
    public Queue smsQueue() {
        return new Queue(appProperties.getRabbit().getQueueSms(), true);
    }

    @Bean
    public Queue statusQueueGateway() {
        return new Queue(appProperties.getRabbit().getQueueStatusGateway(), true);
    }

    // --- EXCHANGES ---

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(
                appProperties.getRabbit().getExchangeNotification(),
                true,
                false
        );
    }

    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(
                appProperties.getRabbit().getExchangeStatus(),
                true,
                false
        );
    }

    // --- BINDINGS ---

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue())
                .to(notificationExchange())
                .with(appProperties.getRabbit().getRoutingSms());
    }

    @Bean
    public Binding statusBinding() {
        return BindingBuilder.bind(statusQueueGateway())
                .to(statusExchange())
                .with("status.*");
    }

    // ---------- Retry + DLQ ----------

    @Bean
    public Declarables smsRetryTopology() {
        return retryTopology(
                appProperties.getRabbit().getSmsRetryQueues(),
                appProperties.getRabbit().getSmsDlq(),
                appProperties.getRabbit().getRoutingSms()
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

    // --- INFRASTRUCTURE ---

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        admin.setIgnoreDeclarationExceptions(true);
        return admin;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        // Важно для поддержки разных типов (уведомления и регистрация)
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 5000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}