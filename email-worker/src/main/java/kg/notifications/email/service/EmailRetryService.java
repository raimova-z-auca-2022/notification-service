package kg.notifications.email.service;

public interface EmailRetryService<T> {
    void handleError(T payload, Integer currentRetryCount, Throwable ex);
}
