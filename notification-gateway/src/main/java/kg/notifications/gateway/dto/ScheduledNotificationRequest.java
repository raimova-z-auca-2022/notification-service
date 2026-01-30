package kg.notifications.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ScheduledNotificationRequest(
        @NotNull NotificationType type,
        @NotBlank String recipient,
        @NotBlank String text,

        @NotNull
        @Future(message = "Время отправки должно быть в будущем с учетом часового пояса")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime scheduledAt
) {}
