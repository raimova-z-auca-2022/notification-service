package kg.notifications.gateway.repository.impl;

import kg.notifications.gateway.dto.ScheduledNotificationEntity;
import kg.notifications.gateway.repository.ScheduledNotificationRepository;
import kg.notifications.gateway.rowMapper.ScheduledNotificationRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduledNotificationRepositoryImpl implements ScheduledNotificationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ScheduledNotificationRowMapper rowMapper;

    @Override
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

    @Override
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

    @Override
    public List<ScheduledNotificationEntity> findByStatusAndScheduledAtBefore(String status, LocalDateTime beforeTime) {
        String sql = """
            SELECT * FROM scheduled_notifications 
            WHERE status = ? AND scheduled_at < ?
            ORDER BY scheduled_at ASC
            """;

        return jdbcTemplate.query(sql, rowMapper, status, beforeTime);
    }

    @Override
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

    @Override
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

}
