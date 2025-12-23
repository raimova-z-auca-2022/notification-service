package kg.notifications.gateway.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertEvent(UUID notificationId,
                            String eventType,
                            String status,
                            Integer attempt,
                            String errorCode,
                            String errorMessage,
                            String providerMessageId) {
        jdbcTemplate.update(
                "INSERT INTO notification_events(notification_id,event_type,status,attempt,error_code,error_message,provider_message_id) VALUES (?,?,?,?,?,?,?)",
                notificationId, eventType, status, attempt, errorCode, errorMessage, providerMessageId
        );
    }
}
