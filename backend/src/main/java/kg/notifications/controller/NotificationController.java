package kg.notifications.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kg.notifications.dto.SendNotificationRequest;
import kg.notifications.dto.SendNotificationResponse;
import kg.notifications.entity.Notification;
import kg.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<SendNotificationResponse> sendEmailNotification(
            @Valid @RequestBody SendNotificationRequest request,
            HttpServletRequest httpRequest
    ) {
        SendNotificationResponse response =
                notificationService.sendEmailNotification(request, httpRequest);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
        Optional<Notification> notificationOpt = notificationService.getNotification(id);
        return notificationOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}