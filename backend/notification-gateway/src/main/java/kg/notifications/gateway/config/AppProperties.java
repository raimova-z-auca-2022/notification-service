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
    private Idempotency idempotency = new Idempotency();
    private Publish publish = new Publish();

    @Data
    public static class Rabbit {
        private String exchangeNotification;
        private String exchangeStatus;

        private String routingEmail;
        private String routingTelegram;
        private String routingWhatsapp = "whatsapp"; // ДОБАВЛЕНО

        private String queueEmail;
        private String queueTelegram;
        private String queueStatusGateway;

        private List<String> emailRetryQueues = new ArrayList<>();
        private String emailDlq;

        private List<String> telegramRetryQueues = new ArrayList<>();
        private String telegramDlq;

        private List<String> whatsappRetryQueues = new ArrayList<>(); // ДОБАВЛЕНО
        private String whatsappDlq; // ДОБАВЛЕНО
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