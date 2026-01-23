package kg.notifications.whatsapp.service;

import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;

public interface DLQService {
    void sendToDLQ(WhatsAppNotificationMessage message, String errorReason);
}