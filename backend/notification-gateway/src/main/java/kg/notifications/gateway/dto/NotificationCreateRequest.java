package kg.notifications.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull NotificationType type,
        @NotBlank String recipient,
        @NotBlank String text
) {}
