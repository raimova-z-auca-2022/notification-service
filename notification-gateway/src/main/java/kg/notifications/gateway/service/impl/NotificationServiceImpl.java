package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.config.AppProperties;
import kg.notifications.gateway.dto.BroadcastRequest; // Импорт
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Желательно добавить транзакционность

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JdbcNotificationRepository notificationRepository;
    private final JdbcNotificationEventRepository eventRepository;
    private final NotificationPublisher publisher;
    private final AppProperties props;

    @Override
    public NotificationCreateResponse create(NotificationCreateRequest request, String idempotencyKey) {
        // Твоя существующая логика создания одиночного уведомления (без изменений)
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
    public void sendBroadcast(BroadcastRequest request, String idempotencyKey) {
        // 1. Валидация (логика теперь здесь, а не в контроллере)
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            throw new BadRequestException("Список получателей не может быть пустым");
        }

        // 2. Генерация общего ID пачки
        String batchId = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        // 3. Логирование (опционально)
        System.out.println(">>> Service: Starting broadcast for " + request.getRecipients().size() + " recipients.");

        // 4. Цикл рассылки
        for (String recipient : request.getRecipients()) {
            // Создаем объект Record через конструктор!
            NotificationCreateRequest singleRequest = new NotificationCreateRequest(
                    request.getType(),
                    recipient,
                    request.getText()
            );

            // Формируем уникальный ключ: "batchId-phone"
            String uniqueKey = batchId + "-" + recipient;

            // Вызываем метод create (реюзаем логику)
            this.create(singleRequest, uniqueKey);
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
