package kg.notifications.sms.exception;

import lombok.Getter;

@Getter
public class SmsApiException extends RuntimeException {
    private final boolean retryable;

    public SmsApiException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public SmsApiException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }
}