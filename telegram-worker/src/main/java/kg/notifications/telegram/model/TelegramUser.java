package kg.notifications.telegram.model;

import lombok.Data;

@Data
public class TelegramUser {
    private Long id;
    private Long chatId;
    private String phoneNumber;
    private String internalUserId;
    private boolean isActive;
}