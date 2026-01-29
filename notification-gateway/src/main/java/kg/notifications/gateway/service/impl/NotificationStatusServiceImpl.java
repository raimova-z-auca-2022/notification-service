package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.dto.StatusEventDto;
import kg.notifications.gateway.repository.impl.JdbcNotificationEventRepository;
import kg.notifications.gateway.repository.impl.JdbcNotificationRepository;
import kg.notifications.gateway.service.NotificationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationStatusServiceImpl implements NotificationStatusService {

    private final JdbcNotificationRepository notificationRepository;
    private final JdbcNotificationEventRepository eventRepository;

    @Override
    public void handleStatusEvent(StatusEventDto evt) {
        UUID id = UUID.fromString(evt.notificationId());

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
    }
}
