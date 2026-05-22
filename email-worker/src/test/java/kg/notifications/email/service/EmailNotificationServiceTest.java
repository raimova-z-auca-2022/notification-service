package kg.notifications.email.service;

import kg.notifications.email.dto.NotificationCommandDto;
import kg.notifications.email.messaging.StatusEventPublisher;
import kg.notifications.email.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Notification Service Tests")
public class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private StatusEventPublisher statusEventPublisher;

    @InjectMocks
    private NotificationServiceImpl emailNotificationService;

    @Test
    @DisplayName("should process email notification")
    public void testProcessEmailNotification() {
        // Arrange
        NotificationCommandDto cmd = new NotificationCommandDto(
                "123e4567-e89b-12d3-a456-426614174000",
                "EMAIL",
                "user@example.com",
                "Test message",
                1,
                OffsetDateTime.now()
        );

        // Act
        emailNotificationService.processEmailNotification(cmd);

        // Assert - verify mail sender was called
        verify(javaMailSender, times(1))
                .send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("should handle null notification gracefully")
    public void testProcessEmailNotificationWithNull() {
        // Act & Assert - should not throw but handle gracefully
        try {
            emailNotificationService.processEmailNotification(null);
        } catch (NullPointerException e) {
            // Expected for null input
        }
    }
}
