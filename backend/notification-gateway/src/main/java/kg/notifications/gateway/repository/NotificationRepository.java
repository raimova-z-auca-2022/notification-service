package kg.notifications.gateway.repository;

import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<NotificationResponse> mapper = new RowMapper<>() {
        @Override
        public NotificationResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new NotificationResponse(
                    rs.getObject("id").toString(),
                    NotificationType.valueOf(rs.getString("type")),
                    rs.getString("recipient"),
                    rs.getString("text"),
                    NotificationStatus.valueOf(rs.getString("status")),
                    rs.getInt("attempt"),
                    rs.getString("last_error_code"),
                    rs.getString("last_error_message"),
                    rs.getString("provider_message_id"),
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };

    public void insertNew(UUID id, NotificationType type, String recipient, String text, String idempotencyKey) {
        jdbcTemplate.update(
                "INSERT INTO notifications(id,type,recipient,text,status,attempt,idempotency_key) VALUES (?,?,?,?, 'NEW', 0, ?)",
                id, type.name(), recipient, text, idempotencyKey
        );
    }

    public Optional<NotificationResponse> findById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM notifications WHERE id = ?", mapper, id)
                .stream().findFirst();
    }

    public Optional<NotificationResponse> findByIdempotencyKey(String idempotencyKey) {
        return jdbcTemplate.query("SELECT * FROM notifications WHERE idempotency_key = ?", mapper, idempotencyKey)
                .stream().findFirst();
    }

    public void markPublished(UUID id) {
        jdbcTemplate.update("UPDATE notifications SET status='PUBLISHED' WHERE id=?", id);
    }

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

    public void markFailed(UUID id, String errorCode, String errorMessage) {
        jdbcTemplate.update(
                "UPDATE notifications SET status='FAILED', last_error_code=?, last_error_message=? WHERE id=?",
                errorCode, errorMessage, id
        );
    }
}
