package kg.notifications.whatsapp.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppProperties appProperties;
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public Queue whatsappQueue() {
        Map<String, Object> args = new HashMap<>();
        // Настраиваем DLQ
        args.put("x-dead-letter-exchange", "x.dead-letter");
        args.put("x-dead-letter-routing-key", appProperties.getQueue().getDlq());
        // Поддержка приоритетов
        args.put("x-max-priority", 10);
        return new Queue(appProperties.getQueue().getName(), true, false, false, args);
    }

    @Bean
    public Queue whatsappDlq() {
        return new Queue(appProperties.getQueue().getDlq(), true);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange("x.notification", true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("x.dead-letter", true, false);
    }

    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange("x.status", true, false);
    }

    @Bean
    public Binding whatsappBinding() {
        return BindingBuilder.bind(whatsappQueue())
                .to(notificationExchange())
                .with("whatsapp");
    }

    @Bean
    public Binding whatsappDlqBinding() {
        return BindingBuilder.bind(whatsappDlq())
                .to(deadLetterExchange())
                .with(appProperties.getQueue().getDlq());
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);

        // Настройка retry на уровне контейнера
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