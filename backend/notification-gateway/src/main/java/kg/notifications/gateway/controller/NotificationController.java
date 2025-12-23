package kg.notifications.gateway.controller;

import jakarta.validation.Valid;
import kg.notifications.gateway.dto.NotificationCreateRequest;
import kg.notifications.gateway.dto.NotificationCreateResponse;
import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotificationCreateResponse create(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
                                             @Valid @RequestBody NotificationCreateRequest request) {
        return notificationService.create(request, idempotencyKey);
    }

    @GetMapping("/notifications/{id}")
    public NotificationResponse getById(@PathVariable UUID id) {
        return notificationService.getById(id);
    }
}
