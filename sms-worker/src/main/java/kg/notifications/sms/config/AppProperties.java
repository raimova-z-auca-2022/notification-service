package kg.notifications.sms.config;

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
        // Twilio
        private String baseUrl;
        private String token;
        private String fromNumber;

        // SMSC.ru
        private String login;
        private String password;
        private boolean testMode;

        private int timeoutMs;
    }

    @Data
    public static class Rabbit {
        private String exchangeNotification;
        private String exchangeStatus;

        private String queueStatusGateway;

        private String routingSms;
        private String queueSms;
        private List<String> smsRetryQueues = new ArrayList<>();
        private String smsDlq;
    }
}