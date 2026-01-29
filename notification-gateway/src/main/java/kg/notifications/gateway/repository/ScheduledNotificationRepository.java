package kg.notifications.gateway.repository;

import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.dto.ScheduledNotificationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduledNotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "scheduled_notifications";

    private final RowMapper<ScheduledNotificationEntity> rowMapper = (rs, rowNum) ->
            new ScheduledNotificationEntity(
                    rs.getString("id"),
                    NotificationType.valueOf(rs.getString("type")),
                    rs.getString("recipient"),
                    rs.getString("text"),
                    rs.getTimestamp("scheduled_at").toLocalDateTime(),
                    rs.getString("status"),
                    rs.getTimestamp("sent_at") != null ?
                            rs.getTimestamp("sent_at").toLocalDateTime() : null,
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
            );

    public void save(ScheduledNotificationEntity entity) {
        String sql = """
            INSERT INTO scheduled_notifications 
            (id, type, recipient, text, scheduled_at, status, sent_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
                entity.id(),
                entity.type().name(),
                entity.recipient(),
                entity.text(),
                entity.scheduledAt(),
                entity.status(),
                entity.sentAt(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    public Optional<ScheduledNotificationEntity> findById(String id) {
        String sql = "SELECT * FROM scheduled_notifications WHERE id = ?";
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, id)
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<ScheduledNotificationEntity> findByStatusAndScheduledAtBefore(
            String status,
            LocalDateTime beforeTime) {
        String sql = """
            SELECT * FROM scheduled_notifications 
            WHERE status = ? AND scheduled_at < ?
            ORDER BY scheduled_at ASC
            """;

        return jdbcTemplate.query(sql, rowMapper, status, beforeTime);
    }

    public List<ScheduledNotificationEntity> findPendingInRange(
            LocalDateTime start,
            LocalDateTime end) {
        String sql = """
            SELECT * FROM scheduled_notifications 
            WHERE status = 'PENDING' 
            AND scheduled_at BETWEEN ? AND ?
            ORDER BY scheduled_at ASC
            """;

        return jdbcTemplate.query(sql, rowMapper, start, end);
    }

    public boolean updateStatus(String id, String status, LocalDateTime sentAt) {
        String sql = """
            UPDATE scheduled_notifications 
            SET status = ?, sent_at = ?, updated_at = ?
            WHERE id = ? AND status != 'CANCELLED'
            """;

        int updated = jdbcTemplate.update(sql,
                status,
                sentAt,
                LocalDateTime.now(),
                id
        );

        return updated > 0;
    }

    public boolean cancelScheduled(String id) {
        String sql = """
            UPDATE scheduled_notifications 
            SET status = 'CANCELLED', updated_at = ?
            WHERE id = ? AND status = 'PENDING'
            """;

        int updated = jdbcTemplate.update(sql,
                LocalDateTime.now(),
                id
        );

        return updated > 0;
    }

    public List<ScheduledNotificationEntity> findByRecipientAndStatus(
            String recipient,
            String status) {
        String sql = """
            SELECT * FROM scheduled_notifications 
            WHERE recipient = ? AND status = ?
            ORDER BY scheduled_at DESC
            """;

        return jdbcTemplate.query(sql, rowMapper, recipient, status);
    }

    public int countPendingByRecipient(String recipient) {
        String sql = """
            SELECT COUNT(*) FROM scheduled_notifications 
            WHERE recipient = ? AND status = 'PENDING'
            """;

        return jdbcTemplate.queryForObject(sql, Integer.class, recipient);
    }
}