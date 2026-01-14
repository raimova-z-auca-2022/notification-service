package kg.notifications.whatsapp.service;

import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;

public interface WhatsAppService {
    void sendMessage(WhatsAppNotificationMessage message);
}