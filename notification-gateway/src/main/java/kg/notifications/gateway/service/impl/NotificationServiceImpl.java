package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.NotificationCreateRequest;
import kg.notifications.gateway.dto.NotificationCreateResponse;
import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.exception.BadRequestException;
import kg.notifications.gateway.exception.NotFoundException;
import kg.notifications.gateway.messaging.NotificationPublisher;
import kg.notifications.gateway.messaging.dto.NotificationCommandDto;
import kg.notifications.gateway.repository.NotificationEventRepository;
import kg.notifications.gateway.repository.NotificationRepository;
import kg.notifications.gateway.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventRepository eventRepository;
    private final NotificationPublisher publisher;
    private final AppProperties props;

    @Override
    public NotificationCreateResponse create(NotificationCreateRequest request, String idempotencyKey) {
        if (props.getIdempotency().isRequired() && (idempotencyKey == null || idempotencyKey.isBlank())) {
            throw new BadRequestException("Idempotency-Key header is required");
        }

        Optional<NotificationResponse> existing = notificationRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new NotificationCreateResponse(existing.get().id());
        }

        UUID id = UUID.randomUUID();
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
            notificationRepository.markFailed(id, "PUBLISH_FAILED", "RabbitMQ publish confirm not received");
            eventRepository.insertEvent(id, "PUBLISH_FAILED", "FAILED", 0, "PUBLISH_FAILED", "RabbitMQ publish confirm not received", null);
            return new NotificationCreateResponse(id.toString());
        }

        notificationRepository.markPublished(id);
        eventRepository.insertEvent(id, "PUBLISHED", "PUBLISHED", 0, null, null, null);

        return new NotificationCreateResponse(id.toString());
    }

    @Override
    public NotificationResponse getById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found: id=" + id));
    }
}
