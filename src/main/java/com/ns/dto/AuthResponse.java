package com.ns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication (login/register)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String message;
    
    public AuthResponse(String token, String username, String email) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.type = "Bearer";
    }
}
