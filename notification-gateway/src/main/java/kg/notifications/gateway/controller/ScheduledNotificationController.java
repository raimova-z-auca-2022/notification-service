package kg.notifications.gateway.controller;

import jakarta.validation.Valid;
import kg.notifications.gateway.dto.ScheduledNotificationRequest;
import kg.notifications.gateway.dto.ScheduledNotificationResponse;
import kg.notifications.gateway.service.ScheduledNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scheduled-notifications")
@RequiredArgsConstructor
public class ScheduledNotificationController {

    private final ScheduledNotificationService scheduledService;

    @PostMapping
    public ResponseEntity<ScheduledNotificationResponse> schedule(
            @Valid @RequestBody ScheduledNotificationRequest request) {
        ScheduledNotificationResponse response = scheduledService.scheduleNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledNotificationResponse> getStatus(@PathVariable String id) {
        ScheduledNotificationResponse response = scheduledService.getStatus(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        boolean cancelled = scheduledService.cancelScheduledMessage(id);
        return cancelled ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ScheduledNotificationResponse>> listPending() {
        List<ScheduledNotificationResponse> response = scheduledService.listPending();
        return ResponseEntity.ok(response);
    }
}