package kg.notifications.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EmailWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(kg.notifications.email.EmailWorkerApplication.class, args);
    }
}