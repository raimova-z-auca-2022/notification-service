package kg.notifications.telegram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationRequest {
    private String token;
    private String internalUserId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}