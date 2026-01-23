package kg.notifications.gateway.service;

import kg.notifications.gateway.dto.StatusEventDto;

public interface NotificationStatusService {

    void handleStatusEvent(StatusEventDto evt);
}
