package com.ns.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для REST API.
 * Обеспечивает единый формат ответов об ошибках.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка ошибок валидации.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (first, second) -> first
                ));

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Validation failed",
                "details", fieldErrors,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Обработка ошибок аутентификации.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationErrors(Exception ex) {
        log.warn("Authentication error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "error", "Invalid username or password",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Обработка IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Обработка RuntimeException.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage());

        // Определяем статус на основе сообщения
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (ex.getMessage().contains("already exists")) {
                status = HttpStatus.CONFLICT;
            } else if (ex.getMessage().contains("Invalid") || ex.getMessage().contains("invalid")) {
                status = HttpStatus.BAD_REQUEST;
            }
        }

        return ResponseEntity.status(status).body(Map.of(
                "success", false,
                "error", ex.getMessage() != null ? ex.getMessage() : "An error occurred",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Обработка всех остальных исключений.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "An unexpected error occurred",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
