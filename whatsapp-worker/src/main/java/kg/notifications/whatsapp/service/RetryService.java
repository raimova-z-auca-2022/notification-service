package kg.notifications.whatsapp.service;


import kg.notifications.whatsapp.dto.NotificationCommandDto;

public interface RetryService {
    void handleError(NotificationCommandDto message, Integer currentRetryCount, Throwable ex);
}