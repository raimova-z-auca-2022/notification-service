package com.ns.service;

import com.ns.dto.AuthResponse;
import com.ns.dto.LoginRequest;
import com.ns.dto.RegisterRequest;
import com.ns.entity.User;
import com.ns.repository.UserRepository;
import com.ns.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис аутентификации.
 * Обрабатывает логин и регистрацию пользователей.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * Аутентификация пользователя и генерация JWT токена.
     * Использует прямую проверку пароля вместо AuthenticationManager
     * для более надёжной работы.
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        // Находим пользователя
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: user '{}' not found", request.getUsername());
                    return new RuntimeException("Invalid username or password");
                });
        
        // Проверяем активность
        if (user.getIsActive() == null || !user.getIsActive()) {
            log.warn("Login failed: user '{}' is inactive", request.getUsername());
            throw new RuntimeException("Account is disabled");
        }
        
        // Проверяем пароль
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for user '{}'", request.getUsername());
            log.debug("Input password: '{}', stored hash: '{}'", request.getPassword(), user.getPassword());
            throw new RuntimeException("Invalid username or password");
        }
        
        // Генерируем JWT токен
        String token = jwtUtil.generateToken(user.getUsername());
        
        log.info("User '{}' logged in successfully", request.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .type("Bearer")
                .build();
    }
    
    /**
     * Регистрация нового пользователя.
     */
    public AuthResponse register(@Valid RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());
        
        // Проверяем уникальность username
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username '{}' already exists", request.getUsername());
            throw new RuntimeException("Username already exists");
        }
        
        // Проверяем уникальность email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email '{}' already exists", request.getEmail());
            throw new RuntimeException("Email already exists");
        }
        
        // Создаём пользователя
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .isActive(true)
                .build();
        
        user = userRepository.save(user);
        
        // Генерируем JWT токен
        String token = jwtUtil.generateToken(user.getUsername());
        
        log.info("User '{}' registered successfully", request.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .type("Bearer")
                .build();
    }
}
