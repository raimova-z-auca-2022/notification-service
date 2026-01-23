package kg.notifications.telegram.repository;

import kg.notifications.telegram.model.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RegistrationRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    // Маппер превращает строку из БД в Java-объект
    private final RowMapper<RegistrationRequest> rowMapper = (rs, rowNum) -> {
        RegistrationRequest req = new RegistrationRequest();
        req.setToken(rs.getString("token"));
        req.setInternalUserId(rs.getString("internal_user_id"));
        req.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        return req;
    };

    public void save(RegistrationRequest request) {
        String sql = "INSERT INTO registration_requests (token, internal_user_id, expires_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, request.getToken(), request.getInternalUserId(), request.getExpiresAt());
    }

    public Optional<RegistrationRequest> findByToken(String token) {
        String sql = "SELECT * FROM registration_requests WHERE token = ?";
        return jdbcTemplate.query(sql, rowMapper, token)
                .stream()
                .findFirst();
    }

    public void deleteByToken(String token) {
        String sql = "DELETE FROM registration_requests WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }
}