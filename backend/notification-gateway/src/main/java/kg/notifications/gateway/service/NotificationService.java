package kg.notifications.gateway.service;

import kg.notifications.gateway.dto.NotificationCreateRequest;
import kg.notifications.gateway.dto.NotificationCreateResponse;
import kg.notifications.gateway.dto.NotificationResponse;

import java.util.UUID;

public interface NotificationService {
    NotificationCreateResponse create(NotificationCreateRequest request, String idempotencyKey);
    NotificationResponse getById(UUID id);
}
