package kg.notifications.service;

import jakarta.servlet.http.HttpServletRequest;
import kg.notifications.dto.SendNotificationRequest;
import kg.notifications.dto.SendNotificationResponse;
import kg.notifications.entity.Notification;

import java.util.Optional;

public interface NotificationService {

    SendNotificationResponse sendEmailNotification(SendNotificationRequest request,
                                                   HttpServletRequest httpRequest);

    Optional<Notification> getNotification(Long id);
}
