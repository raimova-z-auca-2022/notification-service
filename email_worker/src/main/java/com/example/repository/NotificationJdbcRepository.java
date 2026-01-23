package com.example.repository;

import com.example.entity.Notification;
import com.example.enums.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
            n.setRecipient(rs.getString("recipient"));
            n.setSubject(rs.getString("subject"));
            n.setMessageBody(rs.getString("message_body"));
            n.setStatusId((Integer) rs.getObject("status_id"));
            n.setRetryCount((Integer) rs.getObject("retry_count"));
            n.setErrorMessage(rs.getString("error_message"));
            n.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            n.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            n.setSentAt(rs.getTimestamp("sent_at") != null ? rs.getTimestamp("sent_at").toLocalDateTime() : null);
            return n;
        }
    };

    public Optional<Notification> findById(Long id) {
        String sql = "SELECT * FROM notifications WHERE notification_id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public void updateStatus(Long id, NotificationStatus status, String errorMessage, LocalDateTime sentAt) {
        String sql = """
                UPDATE notifications
                SET status_id = ?, error_message = ?, sent_at = ?, updated_at = CURRENT_TIMESTAMP
                WHERE notification_id = ?
                """;
        jdbcTemplate.update(sql,
                status != null ? status.getId() : null,
                errorMessage,
                sentAt,
                id);
    }

    public void incrementRetry(Long id, String errorMessage) {
        String sql = """
                UPDATE notifications
                SET retry_count = COALESCE(retry_count, 0) + 1,
                    error_message = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE notification_id = ?
                """;
        jdbcTemplate.update(sql, errorMessage, id);
    }
}
