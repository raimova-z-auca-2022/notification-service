package kg.notifications.telegram.repository;

import kg.notifications.telegram.model.TelegramUser;

import java.util.List;
import java.util.Optional;

public interface TelegramUserRepository {

    void save(TelegramUser user);

    Optional<TelegramUser> findByChatId(Long chatId);

    List<TelegramUser> findAllByPhoneNumber(String phoneNumber);
}
