package kg.notifications.gateway.repository.impl;

import kg.notifications.gateway.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StatsRepositoryImpl implements StatsRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long countSent() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notifications WHERE status = 'SENT'",
                Long.class
        );
    }

    @Override
    public Long countPending() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notifications WHERE status IN ('NEW','PUBLISHED','PROCESSING')",
                Long.class
        );
    }

    @Override
    public Long countScheduled() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scheduled_notifications WHERE status = 'PENDING'",
                Long.class
        );
    }

    @Override
    public Double avgDeliveryTime() {
        return jdbcTemplate.queryForObject(
                """
                SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)))
                FROM notifications
                WHERE status = 'SENT' AND updated_at IS NOT NULL
                """,
                Double.class
        );
    }

    @Override
    public List<Map<String, Object>> channelDistributionLast7Days() {
        return jdbcTemplate.queryForList(
                """
                SELECT type, COUNT(*) AS cnt
                FROM notifications
                WHERE created_at >= now() - interval '7 days'
                GROUP BY type
                """
        );
    }

    @Override
    public List<Map<String, Object>> channelDistributionOverall() {
        return jdbcTemplate.queryForList(
                "SELECT type, COUNT(*) AS cnt FROM notifications GROUP BY type"
        );
    }
}
