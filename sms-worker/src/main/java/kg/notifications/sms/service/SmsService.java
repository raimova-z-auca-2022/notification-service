package kg.notifications.sms.service;

import kg.notifications.sms.dto.NotificationCommandDto;

public interface SmsService {
    void sendMessage(NotificationCommandDto message);
}