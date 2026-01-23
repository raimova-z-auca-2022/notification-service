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
    private Telegram telegram = new Telegram();
    private Idempotency idempotency = new Idempotency();
    private Publish publish = new Publish();

    @Data
    public static class Rabbit {
        private String exchangeNotification;
        private String exchangeStatus;

        private String routingEmail;
        private String routingTelegram;
        private String routingTelegramRegistration;

        private String queueEmail;
        private String queueTelegram;
        private String queueTelegramRegistration;
        private String queueStatusGateway;

        private List<String> emailRetryQueues = new ArrayList<>();
        private String emailDlq;

        private List<String> telegramRetryQueues = new ArrayList<>();
        private String telegramDlq;
    }

    @Data
    public static class Telegram {
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
