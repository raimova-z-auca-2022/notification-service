package kg.notifications.service;

public interface AsyncNotificationSender {

    void sendAsync(Long notificationId);
}
