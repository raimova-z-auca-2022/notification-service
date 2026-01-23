package kg.notifications.telegram.config;

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
    private LinkCode linkCode = new LinkCode();

    @Data
    public static class Rabbit {
        private String exchangeNotification;
        // private String exchangeStatus; // Можно добавить, если воркер шлет статусы

        private String routingTelegram;
        private String routingTelegramRegistration;

        private String queueTelegram;
        private String queueTelegramRegistration;

        // Для DLQ и Retry (если используем)
        private List<String> telegramRetryQueues = new ArrayList<>();
        private String telegramDlq;
    }

    @Data
    public static class Telegram {
        private String botToken;
        private String botName;
    }

    @Data
    public static class LinkCode {
        private int ttlMinutes = 60;
    }
}