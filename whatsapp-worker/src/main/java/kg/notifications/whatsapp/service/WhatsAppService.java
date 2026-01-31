package kg.notifications.whatsapp.service;

import kg.notifications.whatsapp.dto.NotificationCommandDto;

public interface WhatsAppService {
    void sendMessage(NotificationCommandDto message);
}