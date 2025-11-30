package com.ns.config;

import com.ns.entity.User;
import com.ns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Инициализатор данных при запуске приложения.
 * Создаёт администратора если его нет в БД.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_EMAIL = "admin@notification-service.com";

    @Override
    public void run(String... args) {
        log.info("=== Data Initializer Starting ===");
        initializeAdminUser();
        log.info("=== Data Initializer Complete ===");
    }

    private void initializeAdminUser() {
        try {
            var existingAdmin = userRepository.findByUsername(ADMIN_USERNAME);
            
            if (existingAdmin.isEmpty()) {
                // Создаём нового администратора
                String encodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
                
                User admin = User.builder()
                        .username(ADMIN_USERNAME)
                        .password(encodedPassword)
                        .email(ADMIN_EMAIL)
                        .fullName("System Administrator")
                        .isActive(true)
                        .build();
                
                userRepository.save(admin);
                log.info("✓ Admin user '{}' created successfully", ADMIN_USERNAME);
                log.info("  Login: {} / {}", ADMIN_USERNAME, ADMIN_PASSWORD);
            } else {
                // Админ существует - обновляем пароль для уверенности
                User admin = existingAdmin.get();
                String newEncodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
                
                // Проверяем, нужно ли обновлять пароль
                if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPassword())) {
                    admin.setPassword(newEncodedPassword);
                    userRepository.save(admin);
                    log.info("✓ Admin password updated to '{}'", ADMIN_PASSWORD);
                } else {
                    log.info("✓ Admin user '{}' exists with correct password", ADMIN_USERNAME);
                }
            }
            
            // Финальная проверка
            verifyAdminLogin();
            
        } catch (Exception e) {
            log.error("✗ Failed to initialize admin user: {}", e.getMessage(), e);
        }
    }

    private void verifyAdminLogin() {
        try {
            var admin = userRepository.findByUsername(ADMIN_USERNAME);
            if (admin.isPresent()) {
                boolean passwordValid = passwordEncoder.matches(ADMIN_PASSWORD, admin.get().getPassword());
                if (passwordValid) {
                    log.info("✓ Admin login verified: {} / {} works correctly", ADMIN_USERNAME, ADMIN_PASSWORD);
                } else {
                    log.error("✗ Admin password verification FAILED!");
                    log.error("  Stored hash: {}", admin.get().getPassword());
                }
            }
        } catch (Exception e) {
            log.error("✗ Failed to verify admin login: {}", e.getMessage());
        }
    }
}
