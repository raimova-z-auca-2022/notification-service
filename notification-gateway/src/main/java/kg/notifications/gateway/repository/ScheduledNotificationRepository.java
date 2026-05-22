package kg.notifications.gateway.repository;

import kg.notifications.gateway.dto.ScheduledNotificationEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledNotificationRepository {

    void save(ScheduledNotificationEntity entity);

    Optional<ScheduledNotificationEntity> findById(String id);

    List<ScheduledNotificationEntity> findByStatusAndScheduledAtBefore(String status, LocalDateTime beforeTime);

    List<ScheduledNotificationEntity> findByStatus(String status);

    boolean updateStatus(String id, String status, LocalDateTime sentAt, String providerMessageId);

    boolean cancelScheduled(String id);

}