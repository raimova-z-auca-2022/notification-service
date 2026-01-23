package kg.notifications.email.config;

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

    // --- Конвертер сообщений ---
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- RabbitTemplate для отправки сообщений ---
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true); // важно для уведомления о недоставке
        return template;
    }

    @Bean
    public DirectExchange notificationExchange() {
        // true = durable
        return new DirectExchange(appProperties.getRabbit().getExchangeNotification(), true, false);
    }

    // --- Очереди ---
    @Bean
    public Queue emailQueue() {
        return new Queue(appProperties.getRabbit().getQueueEmail(), true);
    }

    // --- Привязка очереди к обменнику ---
    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(notificationExchange)
                .with(appProperties.getRabbit().getRoutingEmail());
    }

    // --- Опционально: DLQ и retry (как в Gateway) ---
    // Если нужны повторные попытки и DLQ, можно добавить здесь Declarables, TTL и dead-letter
}
