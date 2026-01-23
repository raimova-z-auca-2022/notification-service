package com.example.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EMAIL_QUEUE = "ns.email.q";
    public static final String EMAIL_DLX = "ns.email.dlq";
    public static final String EMAIL_EXCHANGE = "ns.email.x";
    public static final String EMAIL_DLX_EXCHANGE = "ns.email.dlx";

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(EMAIL_DLX_EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_DLX)
                .build();
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(EMAIL_DLX).build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_QUEUE);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(dlq())
                .to(dlxExchange())
                .with(EMAIL_DLX);
    }
}
