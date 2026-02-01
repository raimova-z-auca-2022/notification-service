package kg.notifications.gateway.repository;

import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    void insertNew(UUID id,
                   NotificationType type,
                   String recipient,
                   String text,
                   String idempotencyKey);

    Optional<NotificationResponse> findById(UUID id);

    Optional<NotificationResponse> findByIdempotencyKey(String idempotencyKey);

    void markPublished(UUID id);

    void updateFromStatusEvent(UUID id,
                               NotificationStatus status,
                               int attempt,
                               String errorCode,
                               String errorMessage,
                               String providerMessageId);

    void markFailed(UUID id, String errorCode, String errorMessage);
    List<NotificationResponse> findAll(int limit, int offset, NotificationType type, NotificationStatus status, String recipient);
}
