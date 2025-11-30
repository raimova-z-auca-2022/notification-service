package com.ns.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для проверки состояния сервиса.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcClient jdbcClient;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service");
        health.put("version", "1.0.0");
        health.put("timestamp", LocalDateTime.now().toString());

        // Проверка БД
        try {
            jdbcClient.sql("SELECT 1").query(Integer.class).single();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("databaseError", e.getMessage());
        }

        return ResponseEntity.ok(health);
    }
}
