package kg.notifications.gateway.rowMapper;

import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.dto.ScheduledNotificationEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

@Component
public class ScheduledNotificationRowMapper implements RowMapper<ScheduledNotificationEntity> {

    @Override
    public ScheduledNotificationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ScheduledNotificationEntity(
                rs.getString("id"),
                NotificationType.valueOf(rs.getString("type")),
                rs.getString("recipient"),
                rs.getString("text"),
                rs.getObject("scheduled_at", OffsetDateTime.class),
                rs.getString("status"),
                rs.getTimestamp("sent_at") != null ?
                        rs.getTimestamp("sent_at").toLocalDateTime() : null,
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
