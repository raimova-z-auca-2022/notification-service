package kg.notifications.telegram.config;

import jakarta.annotation.PostConstruct;
import kg.notifications.telegram.service.impl.TelegramServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramBotConfig {

    private final TelegramServiceImpl telegramBotService;

    public TelegramBotConfig(TelegramServiceImpl telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            log.info("✅ Telegram bot successfully registered");
        } catch (Exception e) {
            log.error("❌ Failed to register Telegram bot", e);
        }
    }

}
