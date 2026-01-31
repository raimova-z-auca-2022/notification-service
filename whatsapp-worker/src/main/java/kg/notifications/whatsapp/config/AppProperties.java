package kg.notifications.whatsapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    // Поля должны быть ПРЯМЫМИ наследниками app.whatsapp
    private Api api = new Api();
    private Rabbit rabbit = new Rabbit();

    @Data
    public static class Api {
        private String baseUrl;
        private String token;
        private String fromNumber;
        private int timeoutMs;
    }

    @Data
    public static class Rabbit {
        private String exchangeNotification;
        private String exchangeStatus;

        private String queueStatusGateway;

        private String routingWhatsapp;
        private String queueWhatsapp;
        private List<String> whatsappRetryQueues = new ArrayList<>();
        private String whatsappDlq;
    }
}