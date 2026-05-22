package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.BroadcastRequest;
import kg.notifications.gateway.dto.MultiBroadcastRequest;
import kg.notifications.gateway.dto.NotificationCreateRequest;
import kg.notifications.gateway.dto.NotificationCreateResponse;
import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.exception.BadRequestException;
import kg.notifications.gateway.exception.NotFoundException;
import kg.notifications.gateway.messaging.NotificationPublisher;
import kg.notifications.gateway.dto.NotificationCommandDto;
import kg.notifications.gateway.repository.impl.JdbcNotificationEventRepository;
import kg.notifications.gateway.repository.impl.JdbcNotificationRepository;
import kg.notifications.gateway.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JdbcNotificationRepository notificationRepository;
    private final JdbcNotificationEventRepository eventRepository;
    private final NotificationPublisher publisher;
    private final AppProperties props;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public NotificationCreateResponse create(NotificationCreateRequest request, String idempotencyKey) {
        if (props.getIdempotency().isRequired() && (idempotencyKey == null || idempotencyKey.isBlank())) {
            throw new BadRequestException("Idempotency-Key header is required");
        }

        Optional<NotificationResponse> existingNotification = notificationRepository.findByIdempotencyKey(idempotencyKey);
        if (existingNotification.isPresent()) {
            log.debug("Idempotent create: returning existing notification id={}", existingNotification.get().id());
            return new NotificationCreateResponse(existingNotification.get().id());
        }

        UUID id = UUID.randomUUID();

        // Persist initial notification record and created event
        notificationRepository.insertNew(id, request.type(), request.recipient(), request.text(), idempotencyKey);
        eventRepository.insertEvent(id, "CREATED", "NEW", 0, null, null, null);

        NotificationCommandDto cmd = new NotificationCommandDto(
                id.toString(),
                request.type(),
                request.recipient(),
                request.text(),
                0,
                OffsetDateTime.now()
        );

        boolean published = publisher.publish(cmd);
        if (!published) {
            log.warn("Publish failed for notificationId={}", id);
            notificationRepository.markFailed(id, "PUBLISH_FAILED", "RabbitMQ publish confirm not received");
            eventRepository.insertEvent(id, "PUBLISH_FAILED", "FAILED", 0, "PUBLISH_FAILED", "RabbitMQ publish confirm not received", null);
            return new NotificationCreateResponse(id.toString());
        }

        notificationRepository.markPublished(id);
        eventRepository.insertEvent(id, "PUBLISHED", "PUBLISHED", 0, null, null, null);

        log.debug("Notification {} created and published", id);
        return new NotificationCreateResponse(id.toString());
    }

    @Override
    public void sendBroadcast(BroadcastRequest request, String idempotencyKey) {
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            throw new BadRequestException("Список получателей не может быть пустым");
        }

        String batchId = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        log.info("Starting broadcast for {} recipients (batchId={})", request.getRecipients().size(), batchId);

        for (String recipient : request.getRecipients()) {
            NotificationCreateRequest singleRequest = new NotificationCreateRequest(
                    request.getType(),
                    recipient,
                    request.getText()
            );

            String uniqueKey = batchId + "-" + recipient;
            this.create(singleRequest, uniqueKey);
        }
    }

    @Override
    public void sendMultiBroadcast(MultiBroadcastRequest request, String idempotencyKey) {
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            throw new BadRequestException("Список получателей не может быть пустым");
        }
        if (request.getTypes() == null || request.getTypes().isEmpty()) {
            throw new BadRequestException("Необходимо выбрать хотя бы один канал");
        }

        String batchId = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        log.info("Starting multi-channel broadcast: channels={} recipients={} batchId={}",
                request.getTypes(), request.getRecipients().size(), batchId);

        for (NotificationType type : request.getTypes()) {
            for (String recipient : request.getRecipients()) {
                NotificationCreateRequest singleRequest = new NotificationCreateRequest(
                        type, recipient, request.getText()
                );
                String uniqueKey = batchId + "-" + type.name() + "-" + recipient;
                this.create(singleRequest, uniqueKey);
            }
        }
    }

    @Override
    public List<NotificationResponse> list(int limit, int offset, NotificationType type, NotificationStatus status, String recipient) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        return notificationRepository.findAll(safeLimit, safeOffset, type, status, recipient);
    }

    @Override
    public NotificationResponse getById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found: id=" + id));
    }
}
