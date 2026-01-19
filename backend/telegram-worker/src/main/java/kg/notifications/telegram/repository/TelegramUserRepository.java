package kg.notifications.telegram.repository;

import kg.notifications.telegram.model.TelegramUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TelegramUserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TelegramUser> rowMapper = (rs, rowNum) -> {
        TelegramUser user = new TelegramUser();
        user.setId(rs.getLong("id"));
        user.setChatId(rs.getLong("chat_id"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setInternalUserId(rs.getString("internal_user_id"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    };

    public void save(TelegramUser user) {
        // Простой UPSERT (Insert или Update, если уже есть)
        // Если ID нет — вставляем, если есть — обновляем
        if (user.getId() == null) {
            String sql = """
                INSERT INTO telegram_users (chat_id, phone_number, internal_user_id, is_active)
                VALUES (?, ?, ?, ?)
            """;
            jdbcTemplate.update(sql, user.getChatId(), user.getPhoneNumber(), user.getInternalUserId(), user.isActive());
        } else {
            String sql = "UPDATE telegram_users SET chat_id=?, phone_number=?, is_active=? WHERE id=?";
            jdbcTemplate.update(sql, user.getChatId(), user.getPhoneNumber(), user.isActive(), user.getId());
        }
    }

    public Optional<TelegramUser> findByChatId(Long chatId) {
        String sql = "SELECT * FROM telegram_users WHERE chat_id = ?";
        return jdbcTemplate.query(sql, rowMapper, chatId)
                .stream()
                .findFirst();
    }

    public List<TelegramUser> findAllByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM telegram_users WHERE phone_number = ? AND is_active = true";
        return jdbcTemplate.query(sql, rowMapper, phoneNumber);
    }
}