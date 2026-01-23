package kg.notifications.telegram.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppProperties appProperties;

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // --- Объявление топологии (как в Gateway) ---

    @Bean
    public DirectExchange notificationExchange() {
        // true = durable
        return new DirectExchange(appProperties.getRabbit().getExchangeNotification(), true, false);
    }

    @Bean
    public Queue telegramQueue() {
        return new Queue(appProperties.getRabbit().getQueueTelegram(), true);
    }

    @Bean
    public Queue telegramRegistrationQueue() {
        return new Queue(appProperties.getRabbit().getQueueTelegramRegistration(), true);
    }

    @Bean
    public Binding bindingTelegram(Queue telegramQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(telegramQueue)
                .to(notificationExchange)
                .with(appProperties.getRabbit().getRoutingTelegram());
    }

    @Bean
    public Binding bindingTelegramRegistration(Queue telegramRegistrationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(telegramRegistrationQueue)
                .to(notificationExchange)
                .with(appProperties.getRabbit().getRoutingTelegramRegistration());
    }
}