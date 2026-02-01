package kg.notifications.gateway.controller;

import kg.notifications.gateway.service.TelegramAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramAuthController {

    private final TelegramAuthService telegramAuthService;

    @PostMapping("/generate-link")
    public ResponseEntity<String> generateLink(
            @RequestParam(required = false) String userId) {

        return ResponseEntity.ok(
                telegramAuthService.generateRegistrationLink(userId)
        );
    }
}
