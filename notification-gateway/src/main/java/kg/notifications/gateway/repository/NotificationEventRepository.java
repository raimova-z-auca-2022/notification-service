package kg.notifications.gateway.repository;

import java.util.UUID;

public interface NotificationEventRepository {

    void insertEvent(UUID notificationId,
                     String eventType,
                     String status,
                     Integer attempt,
                     String errorCode,
                     String errorMessage,
                     String providerMessageId);
}
