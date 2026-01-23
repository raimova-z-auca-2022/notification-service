package kg.notifications.email.rowMapper;

import kg.notifications.email.entity.Notification;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationRowMapper implements RowMapper<Notification> {

    @Override
    public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getLong("notification_id"));
        n.setExternalId(rs.getString("external_id"));
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
}

