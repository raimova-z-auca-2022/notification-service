package kg.notifications.email.config;

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

    @Data
    public static class Rabbit {
        private String exchangeNotification;
        private String exchangeStatus;

        private String routingEmail;

        private String queueEmail;
        private String queueStatusGateway;

        private List<String> emailRetryQueues = new ArrayList<>();
        private String emailDlq;

    }

}

