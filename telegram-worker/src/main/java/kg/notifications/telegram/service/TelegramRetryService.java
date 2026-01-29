package kg.notifications.telegram.service;

public interface TelegramRetryService<T> {
    void handleError(T payload, Integer currentRetryCount, Throwable ex);
}