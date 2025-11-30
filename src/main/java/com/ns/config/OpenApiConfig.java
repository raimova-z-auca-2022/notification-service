package com.ns.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration
 * Provides interactive API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("Centralized notification service for multi-channel messaging (Email, SMS, Telegram, WhatsApp)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NS Team")
                                .email("support@notification-service.com")
                                .url("https://github.com/your-org/notification-service"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://notification-service.com/license")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/v1/auth/login")));
    }
}
