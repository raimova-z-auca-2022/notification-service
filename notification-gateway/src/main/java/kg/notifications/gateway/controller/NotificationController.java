package kg.notifications.gateway.controller;

import jakarta.validation.Valid;
import kg.notifications.gateway.dto.BroadcastRequest;
import kg.notifications.gateway.dto.MultiBroadcastRequest;
import kg.notifications.gateway.dto.NotificationCreateRequest;
import kg.notifications.gateway.dto.NotificationCreateResponse;
import kg.notifications.gateway.dto.NotificationResponse;
import kg.notifications.gateway.dto.NotificationStatus;
import kg.notifications.gateway.dto.NotificationType;
import kg.notifications.gateway.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

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


    @GetMapping("/notifications")
    public List<NotificationResponse> list(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) String recipient) {
        return notificationService.list(limit, offset, type, status, recipient);
    }

    @GetMapping("/notifications/{id}")
    public NotificationResponse getById(@PathVariable UUID id) {
        return notificationService.getById(id);
    }

    @PostMapping("/notifications/broadcast")
    public ResponseEntity<String> sendBroadcast(
            @RequestBody BroadcastRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        notificationService.sendBroadcast(request, idempotencyKey);
        return ResponseEntity.ok("Broadcast started for " + request.getRecipients().size() + " recipients.");
    }

    @PostMapping("/notifications/broadcast/multi")
    public ResponseEntity<String> sendMultiBroadcast(
            @RequestBody MultiBroadcastRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        notificationService.sendMultiBroadcast(request, idempotencyKey);
        int total = request.getTypes().size() * request.getRecipients().size();
        return ResponseEntity.ok("Broadcast started: " + request.getTypes().size()
                + " channel(s) × " + request.getRecipients().size()
                + " recipients = " + total + " messages.");
    }
}