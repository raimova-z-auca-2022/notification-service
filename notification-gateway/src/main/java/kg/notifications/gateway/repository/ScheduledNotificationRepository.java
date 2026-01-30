package kg.notifications.gateway.repository;

import kg.notifications.gateway.dto.ScheduledNotificationEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledNotificationRepository {

    void save(ScheduledNotificationEntity entity);

    Optional<ScheduledNotificationEntity> findById(String id);

    List<ScheduledNotificationEntity> findByStatusAndScheduledAtBefore(String status, LocalDateTime beforeTime);

    boolean updateStatus(String id, String status, LocalDateTime sentAt);

    boolean cancelScheduled(String id);

}