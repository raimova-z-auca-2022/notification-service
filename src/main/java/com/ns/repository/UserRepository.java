package com.ns.repository;

import com.ns.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity using JdbcClient
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {
    
    private final JdbcClient jdbcClient;
    
    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        
        return jdbcClient.sql("""
            SELECT user_id, username, password, email, full_name, is_active, created_at, updated_at
            FROM users
            WHERE username = ?
        """)
        .param(username)
        .query((rs, rowNum) -> User.builder()
            .userId(rs.getLong("user_id"))
            .username(rs.getString("username"))
            .password(rs.getString("password"))
            .email(rs.getString("email"))
            .fullName(rs.getString("full_name"))
            .isActive(rs.getBoolean("is_active"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build())
        .optional();
    }
    
    /**
     * Save a new user
     */
    /**
     * Save or update a user
     */
    public User save(User user) {
        log.debug("Saving user: {}", user.getUsername());
        
        if (user.getUserId() != null) {
            // Update existing user
            jdbcClient.sql("""
                UPDATE users 
                SET username = ?, password = ?, email = ?, full_name = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
            """)
            .params(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFullName(),
                user.getIsActive(),
                user.getUserId()
            )
            .update();
            
            log.info("User updated with ID: {}", user.getUserId());
            return user;
        } else {
            // Insert new user
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcClient.sql("""
                INSERT INTO users (username, password, email, full_name, is_active)
                VALUES (?, ?, ?, ?, ?)
            """)
            .params(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFullName(),
                user.getIsActive()
            )
            .update(keyHolder, "user_id");
            
            Long generatedId = keyHolder.getKeyAs(Long.class);
            user.setUserId(generatedId);
            
            log.info("User saved with ID: {}", generatedId);
            return user;
        }
    }
    
    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        log.debug("Checking if username exists: {}", username);
        
        Integer count = jdbcClient.sql("""
            SELECT COUNT(*) FROM users WHERE username = ?
        """)
        .param(username)
        .query(Integer.class)
        .single();
        
        return count > 0;
    }
    
    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        
        Integer count = jdbcClient.sql("""
            SELECT COUNT(*) FROM users WHERE email = ?
        """)
        .param(email)
        .query(Integer.class)
        .single();
        
        return count > 0;
    }
}
