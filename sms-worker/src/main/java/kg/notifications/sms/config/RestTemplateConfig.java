package kg.notifications.sms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(AppProperties appProperties) {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(appProperties.getApi().getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(appProperties.getApi().getTimeoutMs()))
                .additionalInterceptors(loggingInterceptor())
                .build();
    }

    @Bean
    public ClientHttpRequestInterceptor loggingInterceptor() {
        return new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                                ClientHttpRequestExecution execution) throws IOException {
                long startTime = System.currentTimeMillis();

                if (log.isDebugEnabled()) {
                    log.debug("SMS API Request: {} {}",
                            request.getMethod(),
                            request.getURI());
                }

                try {
                    ClientHttpResponse response = execution.execute(request, body);

                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("SMS API Response: {} {} ({}ms)",
                            response.getStatusCode(),
                            request.getURI(),
                            duration);

                    return response;
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("SMS API Error: {} ({}ms)",
                            e.getMessage(), duration, e);
                    throw e;
                }
            }
        };
    }
}