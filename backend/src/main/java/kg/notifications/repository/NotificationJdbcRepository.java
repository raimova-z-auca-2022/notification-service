package kg.notifications.repository;

import kg.notifications.entity.Notification;
import kg.notifications.enums.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Notification> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notification n = new Notification();
            n.setNotificationId(rs.getLong("notification_id"));
            n.setClientId((Integer) rs.getObject("client_id"));
            n.setChannelType(rs.getString("channel_type"));
            n.setRecipient(rs.getString("recipient"));
            n.setSubject(rs.getString("subject"));
            n.setMessageBody(rs.getString("message_body"));
            n.setStatusId((Integer) rs.getObject("status_id"));
            n.setRetryCount((Integer) rs.getObject("retry_count"));
            n.setErrorMessage(rs.getString("error_message"));
            n.setCreatedAt(rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime()
                    : null);
            n.setSentAt(rs.getTimestamp("sent_at") != null
                    ? rs.getTimestamp("sent_at").toLocalDateTime()
                    : null);
            n.setUpdatedAt(rs.getTimestamp("updated_at") != null
                    ? rs.getTimestamp("updated_at").toLocalDateTime()
                    : null);
            n.setExpiresAt(rs.getTimestamp("expires_at") != null
                    ? rs.getTimestamp("expires_at").toLocalDateTime()
                    : null);
            return n;
        }
    };

    public Long insert(Notification n) {
        String sql =
                "INSERT INTO notifications (" +
                        " client_id, channel_type, recipient, subject, message_body, status_id, retry_count, error_message" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)" +
                        " RETURNING notification_id";

        // created_at/updated_at/expires_at заполняются дефолтами на уровне БД
        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
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

    public Optional<Notification> findById(Long id) {
        String sql =
                "SELECT notification_id, client_id, channel_type, recipient, subject, " +
                        "       message_body, status_id, retry_count, error_message, " +
                        "       created_at, sent_at, updated_at, expires_at " +
                        "  FROM notifications " +
                        " WHERE notification_id = ?";

        List<Notification> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.stream().findFirst();
    }
}
