package com.ns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Notification Service
 * Provides centralized notification management across multiple channels
 * 
 * @author NS Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling // For TTL cleanup scheduled task
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Notification Service Started Successfully!");
        System.out.println("Server running on: http://localhost:8080");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("Actuator: http://localhost:8080/actuator/health");
        System.out.println("========================================\n");
    }
}
