package kg.notifications.gateway.repository.impl;

import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.repository.NotificationRepository;
import kg.notifications.gateway.rowMapper.NotificationRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcNotificationRepository
        implements NotificationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NotificationRowMapper mapper = new NotificationRowMapper();

    @Override
    public void insertNew(UUID id, NotificationType type, String recipient, String text, String idempotencyKey) {
        jdbcTemplate.update(
                "INSERT INTO notifications(id,type,recipient,text,status,attempt,idempotency_key) VALUES (?,?,?,?, 'NEW', 0, ?)",
                id, type.name(), recipient, text, idempotencyKey
        );
    }

    @Override
    public Optional<NotificationResponse> findById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM notifications WHERE id = ?", mapper, id)
                .stream().findFirst();
    }

    @Override
    public Optional<NotificationResponse> findByIdempotencyKey(String idempotencyKey) {
        return jdbcTemplate.query("SELECT * FROM notifications WHERE idempotency_key = ?", mapper, idempotencyKey)
                .stream().findFirst();
    }

    @Override
    public void markPublished(UUID id) {
        jdbcTemplate.update("UPDATE notifications SET status='PUBLISHED' WHERE id=?", id);
    }

    @Override
    public void updateFromStatusEvent(UUID id,
                                      NotificationStatus status,
                                      int attempt,
                                      String errorCode,
                                      String errorMessage,
                                      String providerMessageId) {
        jdbcTemplate.update(
                "UPDATE notifications SET status=?, attempt=?, last_error_code=?, last_error_message=?, provider_message_id=? WHERE id=?",
                status.name(), attempt, errorCode, errorMessage, providerMessageId, id
        );
    }

    @Override
    public void markFailed(UUID id, String errorCode, String errorMessage) {
        jdbcTemplate.update(
                "UPDATE notifications SET status='FAILED', last_error_code=?, last_error_message=? WHERE id=?",
                errorCode, errorMessage, id
        );
    }
}
