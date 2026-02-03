package kg.notifications.gateway.exception;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        String error,
        OffsetDateTime timestamp
) {}
