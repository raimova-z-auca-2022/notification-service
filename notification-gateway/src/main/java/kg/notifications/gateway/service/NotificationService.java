package kg.notifications.gateway.service;

import kg.notifications.gateway.dto.BroadcastRequest; // Импорт DTO
import kg.notifications.gateway.dto.NotificationCreateRequest;
import kg.notifications.gateway.dto.NotificationCreateResponse;
import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;

import java.util.UUID;
import java.util.List;

public interface NotificationService {
    NotificationCreateResponse create(NotificationCreateRequest request, String idempotencyKey);
    NotificationResponse getById(UUID id);

    // Добавили метод для рассылки
    void sendBroadcast(BroadcastRequest request, String idempotencyKey);
    List<NotificationResponse> list(int limit, int offset, NotificationType type, NotificationStatus status, String recipient);
}
