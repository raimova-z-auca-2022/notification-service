package kg.notifications.telegram.rowMapper;

import kg.notifications.telegram.model.TelegramUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TelegramUserRowMapper implements RowMapper<TelegramUser> {

    @Override
    public TelegramUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        TelegramUser user = new TelegramUser();
        user.setId(rs.getLong("id"));
        user.setChatId(rs.getLong("chat_id"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setInternalUserId(rs.getString("internal_user_id"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}
