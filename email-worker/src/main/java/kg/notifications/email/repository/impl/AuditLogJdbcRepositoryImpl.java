package kg.notifications.email.repository.impl;

import kg.notifications.email.entity.AuditLog;
import kg.notifications.email.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogJdbcRepositoryImpl implements AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insert(AuditLog log) {
        String sql =
                "INSERT INTO audit_log (" +
                        " action_type, entity_type, entity_id, details, created_at" +
                        ") VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                log.getActionType(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
