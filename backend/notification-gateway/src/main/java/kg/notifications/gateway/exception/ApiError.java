package kg.notifications.gateway.exception;

import java.time.OffsetDateTime;

public record ApiError(
        String message,
        String code,
        OffsetDateTime timestamp
) {}
