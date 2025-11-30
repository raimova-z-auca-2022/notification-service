package com.ns.controller;

import com.ns.dto.AuthResponse;
import com.ns.dto.LoginRequest;
import com.ns.dto.RegisterRequest;
import com.ns.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Контроллер аутентификации.
 * Обрабатывает эндпоинты логина и регистрации.
 */
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Эндпоинт логина.
     * POST /api/v1/auth/login
     */
    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("=== LOGIN REQUEST ===");
        log.info("Username: '{}'", request.getUsername());
        log.debug("Password length: {}", request.getPassword() != null ? request.getPassword().length() : 0);
        
        try {
            AuthResponse response = authService.login(request);
            log.info("=== LOGIN SUCCESS for '{}' ===", request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("=== LOGIN FAILED ===");
            log.error("Error: {}", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "message", "Invalid username or password",
                        "error", e.getMessage()
                    ));
        }
    }
    
    /**
     * Эндпоинт регистрации.
     * POST /api/v1/auth/register
     */
    @Operation(summary = "User registration", description = "Register new user and get JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully registered"),
        @ApiResponse(responseCode = "400", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for user: {}", request.getUsername());
        
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                    ));
        }
    }
    
    /**
     * Health check эндпоинт для проверки работы auth сервиса.
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "auth",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
