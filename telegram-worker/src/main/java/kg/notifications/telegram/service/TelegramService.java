package kg.notifications.telegram.service;

import kg.notifications.telegram.dto.NotificationCommandDto;
import kg.notifications.telegram.dto.RegistrationCommandDto;

public interface TelegramService {

    void handleRegistration(RegistrationCommandDto cmd);

    void sendNotification(NotificationCommandDto dto);
}
