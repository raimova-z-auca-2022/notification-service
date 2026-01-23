package kg.notifications.whatsapp.exception;

import lombok.Getter;

@Getter
public class WhatsAppApiException extends RuntimeException {
    private final boolean retryable;

    public WhatsAppApiException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public WhatsAppApiException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }
}