package kg.notifications.email.repository;

import kg.notifications.email.entity.Notification;
import kg.notifications.email.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationJdbcRepository {

    Long insert(Notification notification);

    void updateStatus(Long id, NotificationStatus status, String errorMessage, LocalDateTime sentAt);

    Optional<Notification> findById(Long id);
}
