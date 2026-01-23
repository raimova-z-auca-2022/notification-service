package kg.notifications.email.service;

import kg.notifications.email.dto.NotificationCommandDto;

public interface NotificationService {
    void processEmailNotification(NotificationCommandDto cmd);
}
