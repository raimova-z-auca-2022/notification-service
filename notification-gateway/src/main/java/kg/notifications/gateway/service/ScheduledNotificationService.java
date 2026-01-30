package kg.notifications.gateway.service;

import kg.notifications.gateway.dto.ScheduledNotificationRequest;
import kg.notifications.gateway.dto.ScheduledNotificationResponse;
import kg.notifications.gateway.dto.ScheduledNotificationEntity;

import java.util.List;

public interface ScheduledNotificationService {
    ScheduledNotificationResponse scheduleNotification(ScheduledNotificationRequest request);
    ScheduledNotificationResponse getStatus(String id);
    boolean cancelScheduledMessage(String id);
    List<ScheduledNotificationResponse> listPending();
    void sendToNotificationQueue(ScheduledNotificationEntity entity);
}