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

    private Api api = new Api();
    private Queue queue = new Queue();
    private Retry retry = new Retry();

    @Data
    public static class Api {
        private String baseUrl;
        private String token;
        private int timeoutMs = 30000;
        private int maxRetries = 3;
    }

    @Data
    public static class Queue {
        private String name = "ns.whatsapp.q";
        private String dlq = "ns.whatsapp.dlq";
    }

    @Data
    public static class Retry {
        private List<String> delays = new ArrayList<>();
    }
}