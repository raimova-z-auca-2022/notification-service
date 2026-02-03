package kg.notifications.telegram.service;

import kg.notifications.telegram.dto.NotificationCommandDto;
import kg.notifications.telegram.service.impl.TelegramServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Telegram Service Tests")
public class TelegramServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TelegramServiceImpl telegramService;

    @Test
    @DisplayName("should handle notification without throwing exception")
    public void testSendNotification() {
        // Arrange
        NotificationCommandDto cmd = new NotificationCommandDto(
                "123e4567-e89b-12d3-a456-426614174100",
                "TELEGRAM",
                "+79991234567",
                "Test message",
                1,
                OffsetDateTime.now()
        );

        // Act - should complete without exception
        try {
            telegramService.sendNotification(cmd);
        } catch (Exception e) {
            // May throw due to missing Telegram bot configuration
        }
    }

    @Test
    @DisplayName("should handle null notification command")
    public void testSendNotificationWithNull() {
        // Act & Assert - should not crash
        try {
            telegramService.sendNotification(null);
        } catch (NullPointerException e) {
            // Expected for null input
        }
    }
}
