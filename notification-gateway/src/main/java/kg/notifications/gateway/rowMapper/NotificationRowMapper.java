package kg.notifications.gateway.rowMapper;

import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class NotificationRowMapper implements RowMapper<NotificationResponse> {

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
}
