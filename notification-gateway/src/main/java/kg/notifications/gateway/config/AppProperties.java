package kg.notifications.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Rabbit rabbit = new Rabbit();
    private Telegram telegram = new Telegram(); // От Айгерим
    private Idempotency idempotency = new Idempotency();
    private Publish publish = new Publish();

    @Data
    public static class Rabbit {
        private String exchangeNotification;
        private String exchangeStatus;
        private String exchangeDelayed; // От Айгерим (для планировщика)

        private String routingEmail;
        private String routingTelegram;
        private String routingSms;
        private String routingTelegramRegistration; // От Айгерим

        private String queueEmail;
        private String queueTelegram;
        private String queueSms;
        private String queueTelegramRegistration; // От Айгерим
        private String queueStatusGateway;

        private String routingScheduled; // От Айгерим
        private String queueScheduledMessages; // От Айгерим

        private List<String> emailRetryQueues = new ArrayList<>();
        private String emailDlq;

        private List<String> telegramRetryQueues = new ArrayList<>();
        private String telegramDlq;

        private List<String> smsRetryQueues = new ArrayList<>();
        private String smsDlq;
    }

    @Data
    public static class Telegram { // Новый блок от Айгерим
        private String botName;
        private String botToken;
    }

    @Data
    public static class Idempotency {
        private boolean required = true;
    }

    @Data
    public static class Publish {
        private long confirmTimeoutMs = 3000;
    }
}