package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.NotificationCreateRequest;
import kg.notifications.gateway.dto.NotificationCreateResponse;
import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.exception.BadRequestException;
import kg.notifications.gateway.exception.NotFoundException;
import kg.notifications.gateway.messaging.NotificationPublisher;
import kg.notifications.gateway.repository.impl.JdbcNotificationEventRepository;
import kg.notifications.gateway.repository.impl.JdbcNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Unit Tests")
public class NotificationServiceImplTest {

    @Mock
    private JdbcNotificationRepository notificationRepository;

    @Mock
    private JdbcNotificationEventRepository eventRepository;

    @Mock
    private NotificationPublisher publisher;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Idempotency idempotency;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    public void setUp() {
        lenient().when(appProperties.getIdempotency()).thenReturn(idempotency);
        lenient().when(idempotency.isRequired()).thenReturn(true);
    }

    @Test
    @DisplayName("create() should throw BadRequestException when idempotency key is required but missing")
    public void testCreateWithoutIdempotencyKey() {
        NotificationCreateRequest request = new NotificationCreateRequest(
                NotificationType.EMAIL,
                "test@example.com",
                "Test message"
        );

        assertThrows(BadRequestException.class, () -> {
            notificationService.create(request, null);
        });
    }

    @Test
    @DisplayName("create() should return existing notification if idempotency key matches")
    public void testCreateIdempotent() {
        String idempotencyKey = "test-key";
        String existingId = UUID.randomUUID().toString();
        NotificationCreateRequest request = new NotificationCreateRequest(
                NotificationType.EMAIL,
                "test@example.com",
                "Test message"
        );

        when(notificationRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(new kg.notifications.gateway.dto.NotificationResponse(
                        existingId, NotificationType.EMAIL, "test@example.com", "Test message",
                        kg.notifications.gateway.dto.NotificationStatus.NEW, 0, null, null, null,
                        null, null
                )));

        NotificationCreateResponse response = notificationService.create(request, idempotencyKey);

        assertEquals(existingId, response.notificationId());
        verify(notificationRepository).findByIdempotencyKey(idempotencyKey);
        verify(notificationRepository, never()).insertNew(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("create() should publish notification successfully")
    public void testCreateSuccess() {
        String idempotencyKey = "test-key";
        NotificationCreateRequest request = new NotificationCreateRequest(
                NotificationType.EMAIL,
                "test@example.com",
                "Test message"
        );

        when(notificationRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(publisher.publish(any())).thenReturn(true);

        NotificationCreateResponse response = notificationService.create(request, idempotencyKey);

        assertNotNull(response.notificationId());
        verify(notificationRepository).insertNew(any(), eq(NotificationType.EMAIL), eq("test@example.com"), eq("Test message"), eq(idempotencyKey));
        verify(eventRepository, atLeast(2)).insertEvent(any(), any(), any(), anyInt(), any(), any(), any());
        verify(publisher).publish(any());
    }

    @Test
    @DisplayName("getById() should throw NotFoundException for non-existent notification")
    public void testGetByIdNotFound() {
        UUID id = UUID.randomUUID();

        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            notificationService.getById(id);
        });
    }

    @Test
    @DisplayName("list() should respect limit boundaries")
    public void testListWithLimitBoundaries() {
        when(notificationRepository.findAll(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(java.util.Collections.emptyList());

        // Should clamp limit to [1, 200]
        notificationService.list(1000, 0, null, null, null);

        verify(notificationRepository).findAll(200, 0, null, null, null);
    }

    @Test
    @DisplayName("sendBroadcast() should throw BadRequestException for empty recipients")
    public void testBroadcastWithEmptyRecipients() {
        kg.notifications.gateway.dto.BroadcastRequest request = new kg.notifications.gateway.dto.BroadcastRequest();
        request.setRecipients(java.util.Collections.emptyList());

        assertThrows(BadRequestException.class, () -> {
            notificationService.sendBroadcast(request, "batch-key");
        });
    }
}
