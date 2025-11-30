package com.ns.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a system user (for admin panel)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
