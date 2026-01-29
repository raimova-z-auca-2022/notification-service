package kg.notifications.whatsapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.whatsapp")
@Data
public class AppProperties {
    // Поля должны быть ПРЯМЫМИ наследниками app.whatsapp
    private Api api = new Api();
    private Queue queue = new Queue();
    private Retry retry = new Retry();

    @Data
    public static class Api {
        private String baseUrl;
        private String token;
        private String fromNumber; // Spring замапит from-number сюда
        private int timeoutMs;
    }

    @Data
    public static class Queue {
        private String name;
        private String dlq;
    }

    @Data
    public static class Retry {
        private List<String> delays = new ArrayList<>();
    }
}