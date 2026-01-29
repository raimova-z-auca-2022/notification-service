package kg.notifications.whatsapp.service;

import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;

public interface NotificationStatusService {
    void sendProcessingStatus(WhatsAppNotificationMessage message);
    void sendSuccessStatus(WhatsAppNotificationMessage message, String messageId);
    void sendErrorStatus(WhatsAppNotificationMessage message, String errorMessage);
}