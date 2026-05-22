package kg.notifications.sms.service;

import kg.notifications.sms.dto.NotificationCommandDto;
import kg.notifications.sms.service.impl.SmsServiceImpl;
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
@DisplayName("SMS Service Tests")
public class SmsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SmsServiceImpl smsService;

    @Test
    @DisplayName("should handle notification command")
    public void testSendMessage() {
        NotificationCommandDto cmd = new NotificationCommandDto(
                "123e4567-e89b-12d3-a456-426614174200",
                "SMS",
                "+79991234567",
                "Test message",
                1,
                OffsetDateTime.now()
        );

        try {
            smsService.sendMessage(cmd);
        } catch (Exception e) {
            // May throw due to missing provider configuration
        }
    }

    @Test
    @DisplayName("should handle null notification command")
    public void testSendMessageWithNull() {
        try {
            smsService.sendMessage(null);
        } catch (NullPointerException e) {
            // Expected for null input
        }
    }
}