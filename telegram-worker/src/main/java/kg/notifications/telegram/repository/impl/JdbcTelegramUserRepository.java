package kg.notifications.telegram.repository.impl;

import kg.notifications.telegram.model.TelegramUser;
import kg.notifications.telegram.repository.TelegramUserRepository;
import kg.notifications.telegram.rowMapper.TelegramUserRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcTelegramUserRepository implements TelegramUserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TelegramUser> rowMapper =
            new TelegramUserRowMapper();

    @Override
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

    @Override
    public Optional<TelegramUser> findByChatId(Long chatId) {
        String sql = "SELECT * FROM telegram_users WHERE chat_id = ?";
        return jdbcTemplate.query(sql, rowMapper, chatId)
                .stream()
                .findFirst();
    }

    @Override
    public List<TelegramUser> findAllByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM telegram_users WHERE phone_number = ? AND is_active = true";
        return jdbcTemplate.query(sql, rowMapper, phoneNumber);
    }
}