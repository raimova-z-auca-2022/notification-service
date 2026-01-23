package kg.notifications.email.repository.impl;

import kg.notifications.email.entity.Notification;
import kg.notifications.email.enums.NotificationStatus;
import kg.notifications.email.repository.NotificationJdbcRepository;
import kg.notifications.email.rowMapper.NotificationRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationJdbcRepositoryImpl implements NotificationJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NotificationRowMapper rowMapper = new NotificationRowMapper();

    @Override
    public Long insert(Notification n) {
        String sql =
                "INSERT INTO notifications (" +
                        " external_id, client_id, channel_type, recipient, subject, message_body, status_id, retry_count, error_message" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING notification_id";

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                n.getExternalId(),
                n.getClientId(),
                n.getChannelType(),
                n.getRecipient(),
                n.getSubject(),
                n.getMessageBody(),
                n.getStatusId(),
                n.getRetryCount(),
                n.getErrorMessage()
        );
    }

    @Override
    public void updateStatus(Long id, NotificationStatus status, String errorMessage, LocalDateTime sentAt) {
        String sql =
                "UPDATE notifications " +
                        "   SET status_id = ?, " +
                        "       error_message = ?, " +
                        "       sent_at = ?, " +
                        "       updated_at = CURRENT_TIMESTAMP " +
                        " WHERE notification_id = ?";

        jdbcTemplate.update(
                sql,
                status != null ? status.getId() : null,
                errorMessage,
                sentAt,
                id
        );
    }

    @Override
    public Optional<Notification> findById(Long id) {
        String sql =
                "SELECT notification_id, external_id, client_id, channel_type, recipient, subject, " +
                        "message_body, status_id, retry_count, error_message, created_at, sent_at, updated_at, expires_at " +
                        "FROM notifications WHERE notification_id = ?";

        List<Notification> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.stream().findFirst();
    }
}
