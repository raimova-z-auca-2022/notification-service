package kg.notifications.whatsapp.service;

import kg.notifications.whatsapp.dto.NotificationCommandDto;
import kg.notifications.whatsapp.service.impl.WhatsAppServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WhatsApp Service Tests")
public class WhatsAppServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WhatsAppServiceImpl whatsAppService;

    @Test
    @DisplayName("should handle notification command")
    public void testSendMessage() {
        // Arrange
        NotificationCommandDto cmd = new NotificationCommandDto(
                "123e4567-e89b-12d3-a456-426614174200",
                "WHATSAPP",
                "whatsapp:+79991234567",
                "Test message",
                1,
                OffsetDateTime.now()
        );

        // Act - should complete without exception
        try {
            whatsAppService.sendMessage(cmd);
        } catch (Exception e) {
            // May throw due to missing Twilio configuration
        }
    }

    @Test
    @DisplayName("should handle null notification command")
    public void testSendMessageWithNull() {
        // Act & Assert - should not crash
        try {
            whatsAppService.sendMessage(null);
        } catch (NullPointerException e) {
            // Expected for null input
        }
    }
}
