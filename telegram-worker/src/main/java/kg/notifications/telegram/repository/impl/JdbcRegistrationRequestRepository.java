package kg.notifications.telegram.repository.impl;

import kg.notifications.telegram.model.RegistrationRequest;
import kg.notifications.telegram.repository.RegistrationRequestRepository;
import kg.notifications.telegram.rowMapper.RegistrationRequestRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcRegistrationRequestRepository
        implements RegistrationRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<RegistrationRequest> rowMapper =
            new RegistrationRequestRowMapper();

    @Override
    public void save(RegistrationRequest request) {
        String sql = "INSERT INTO registration_requests (token, internal_user_id, expires_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, request.getToken(), request.getInternalUserId(), request.getExpiresAt());
    }

    @Override
    public Optional<RegistrationRequest> findByToken(String token) {
        String sql = "SELECT * FROM registration_requests WHERE token = ?";
        return jdbcTemplate.query(sql, rowMapper, token)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteByToken(String token) {
        String sql = "DELETE FROM registration_requests WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }
}