package kg.notifications.email.service;

import kg.notifications.email.entity.Notification;

public interface EmailService {
    void sendEmail(Notification notification);
}
