package kg.notifications.sms.service;


import kg.notifications.sms.dto.NotificationCommandDto;

public interface RetryService {
    void handleError(NotificationCommandDto message, Integer currentRetryCount, Throwable ex);
}