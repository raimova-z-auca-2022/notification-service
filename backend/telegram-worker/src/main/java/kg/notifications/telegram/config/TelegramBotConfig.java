package kg.notifications.telegram.config;

import kg.notifications.telegram.service.TelegramService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramBotConfig {

    private final TelegramService telegramService;

    public TelegramBotConfig(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi botsApi =
                    new TelegramBotsApi(DefaultBotSession.class);

            botsApi.registerBot(telegramService);

            log.info("✅ Telegram bot successfully registered");

        } catch (Exception e) {
            log.error("❌ Failed to register Telegram bot", e);
        }
    }
}
