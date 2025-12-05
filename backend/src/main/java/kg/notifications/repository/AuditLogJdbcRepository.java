package kg.notifications.repository;

import kg.notifications.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insert(AuditLog log) {
        String sql =
                "INSERT INTO audit_log (" +
                        " action_type, entity_type, entity_id, details, ip_address, user_agent, created_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                log.getActionType(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }
}
