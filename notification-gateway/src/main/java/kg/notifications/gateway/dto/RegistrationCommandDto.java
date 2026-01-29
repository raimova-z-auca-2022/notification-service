package kg.notifications.gateway.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record RegistrationCommandDto(
        String linkCode,       // Уникальный токен
        String internalUserId, // ID юзера в твоей системе (или null)
        LocalDateTime expiresAt // Когда ссылка протухнет
) implements Serializable {}
