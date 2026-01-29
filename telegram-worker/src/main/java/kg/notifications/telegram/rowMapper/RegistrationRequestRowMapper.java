package kg.notifications.telegram.rowMapper;

import kg.notifications.telegram.model.RegistrationRequest;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationRequestRowMapper
        implements RowMapper<RegistrationRequest> {

    @Override
    public RegistrationRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
        RegistrationRequest req = new RegistrationRequest();
        req.setToken(rs.getString("token"));
        req.setInternalUserId(rs.getString("internal_user_id"));
        req.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        return req;
    }
}
