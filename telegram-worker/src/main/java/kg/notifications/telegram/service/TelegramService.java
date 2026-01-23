package kg.notifications.telegram.service;

import kg.notifications.telegram.config.AppProperties;
import kg.notifications.telegram.dto.NotificationCommandDto;
import kg.notifications.telegram.model.RegistrationRequest;
import kg.notifications.telegram.model.TelegramUser;
import kg.notifications.telegram.repository.RegistrationRequestRepository;
import kg.notifications.telegram.repository.TelegramUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TelegramService extends TelegramLongPollingBot {

    private final AppProperties appProperties;
    private final RegistrationRequestRepository registrationRepo;
    private final TelegramUserRepository userRepo;

    public TelegramService(AppProperties appProperties,
                           RegistrationRequestRepository registrationRepo,
                           TelegramUserRepository userRepo) {
        super(new DefaultBotOptions());
        this.appProperties = appProperties;
        this.registrationRepo = registrationRepo;
        this.userRepo = userRepo;
    }

    @Override
    public String getBotToken() {
        return appProperties.getTelegram().getBotToken();
    }

    @Override
    public String getBotUsername() {
        return appProperties.getTelegram().getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();

        if (message.hasText() && message.getText().startsWith("/start")) {
            handleStartCommand(chatId, message.getText());
        } else if (message.hasContact()) {
            handleContact(chatId, message.getContact());
        }
    }

    // --- Отправка уведомлений (вызывается из Rabbit listener) ---
    public void sendNotification(NotificationCommandDto dto) {
        String normalizedPhone = normalizePhone(dto.recipient());

        List<TelegramUser> users = userRepo.findAllByPhoneNumber(normalizedPhone);

        if (users.isEmpty()) {
            log.warn(
                    "Notification {} skipped: no Telegram user for phone {}",
                    dto.notificationId(),
                    normalizedPhone
            );
            return;
        }

        for (TelegramUser user : users) {
            try {
                execute(new SendMessage(user.getChatId().toString(), dto.text()));
                log.info(
                        "Notification {} sent to chatId {}",
                        dto.notificationId(),
                        user.getChatId()
                );
            } catch (TelegramApiException e) {
                log.error(
                        "Failed to send notification {} to chatId {}",
                        dto.notificationId(),
                        user.getChatId(),
                        e
                );
            }
        }
    }

    private void handleStartCommand(Long chatId, String text) {
        String[] parts = text.split(" ");
        if (parts.length != 2) {
            sendText(chatId, "Пожалуйста, используйте ссылку из приложения.");
            return;
        }

        String token = parts[1];
        Optional<RegistrationRequest> reqOpt = registrationRepo.findByToken(token);

        if (reqOpt.isEmpty() || reqOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            sendText(chatId, "⛔ Ссылка недействительна или устарела.");
            return;
        }

        sendContactRequestButton(chatId);
    }

    private void handleContact(Long chatId, Contact contact) {
        if (!contact.getUserId().equals(chatId)) {
            sendText(chatId, "⛔ Пожалуйста, отправьте СВОЙ контакт.");
            return;
        }

        String phone = normalizePhone(contact.getPhoneNumber());

        TelegramUser user = userRepo.findByChatId(chatId)
                .orElseGet(TelegramUser::new);

        user.setChatId(chatId);
        user.setPhoneNumber(phone);
        user.setActive(true);

        userRepo.save(user);

        sendTextWithRemoveKeyboard(
                chatId,
                "✅ Номер " + phone + " успешно привязан!"
        );
    }

    private void sendContactRequestButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Нажмите кнопку ниже, чтобы подтвердить номер 👇");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        KeyboardButton button = new KeyboardButton("📱 Подтвердить номер");
        button.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(button);

        keyboardMarkup.setKeyboard(List.of(row));
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send contact request button", e);
        }
    }

    private void sendText(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    private void sendTextWithRemoveKeyboard(Long chatId, String text) {
        SendMessage msg = new SendMessage(chatId.toString(), text);
        msg.setReplyMarkup(new ReplyKeyboardRemove(true));

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9]", "");
    }
}
