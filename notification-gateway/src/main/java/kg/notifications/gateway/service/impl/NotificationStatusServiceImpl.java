package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.dto.StatusEventDto;
import kg.notifications.gateway.repository.impl.JdbcNotificationEventRepository;
import kg.notifications.gateway.repository.impl.JdbcNotificationRepository;
import kg.notifications.gateway.repository.ScheduledNotificationRepository;
import kg.notifications.gateway.service.NotificationStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationStatusServiceImpl implements NotificationStatusService {

    private final JdbcNotificationRepository notificationRepository;
    private final JdbcNotificationEventRepository eventRepository;
    private final ScheduledNotificationRepository scheduledRepository;

    @Override
    @Transactional
    public void handleStatusEvent(StatusEventDto evt) {
        UUID id = UUID.fromString(evt.notificationId());

        log.debug("Applying status event for notificationId={} status={}", id, evt.status());

        notificationRepository.updateFromStatusEvent(
                id,
                evt.status(),
                evt.attempt(),
                evt.errorCode(),
                evt.errorMessage(),
                evt.providerMessageId()
        );

        eventRepository.insertEvent(
                id,
                "STATUS_CHANGED",
                evt.status().name(),
                evt.attempt(),
                evt.errorCode(),
                evt.errorMessage(),
                evt.providerMessageId()
        );

        // Если это id относится к scheduled_notifications — обновим и её
        try {
            String providerId = evt.providerMessageId();
            if (providerId != null && !providerId.isEmpty()) {
                // Попытка обновить scheduled record (если существует)
                scheduledRepository.updateStatus(evt.notificationId(), evt.status().name(), null, providerId);
            } else {
                scheduledRepository.updateStatus(evt.notificationId(), evt.status().name(), null, null);
            }
        } catch (Exception e) {
            log.debug("Scheduled notification update skipped or failed for id={}: {}", evt.notificationId(), e.getMessage());
        }
    }
}
