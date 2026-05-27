package kg.notifications.sms.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kg.notifications.sms.config.AppProperties;
import kg.notifications.sms.dto.NotificationCommandDto;
import kg.notifications.sms.exception.InvalidMessageException;
import kg.notifications.sms.exception.SmsApiException;
import kg.notifications.sms.listener.StatusEventPublisher;
import kg.notifications.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RestTemplate restTemplate;
    private final AppProperties props;
    private final ObjectMapper objectMapper;
    private final StatusEventPublisher statusService;

    @Override
    @Transactional
    public void sendMessage(NotificationCommandDto message) {
        if (message == null) {
            log.warn("sendMessage called with null message");
            return;
        }

        log.debug("Starting SMS send for notification: {}", message.notificationId());

        if (props.getApi().isTestMode()) {
            log.info("[TEST MODE] Skipping real SMS to {}, marking as SENT", message.recipient());
            statusService.publishSent(message, "test-" + UUID.randomUUID());
            return;
        }

        boolean hasSmsc = isSet(props.getApi().getLogin()) && isSet(props.getApi().getPassword());
        boolean hasTextbelt = isSet(props.getApi().getTextbeltKey());
        boolean hasTwilio = isSet(props.getApi().getBaseUrl());

        if (!hasSmsc && !hasTextbelt && !hasTwilio) {
            log.info("[MOCK] SMS to {} — no provider configured, marking as SENT", message.recipient());
            statusService.publishSent(message, "mock-" + UUID.randomUUID());
            return;
        }

        try {
            validateMessage(message);

            if (hasTextbelt) {
                sendViaTextbelt(message);
            } else if (hasSmsc) {
                sendViaSmsc(message);
            } else {
                sendViaTwilio(message);
            }

        } catch (HttpClientErrorException e) {
            log.error("SMS API Client Error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SmsApiException("SMS API client error: " + e.getStatusCode(),
                    e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS);
        } catch (HttpServerErrorException e) {
            log.error("SMS API Server Error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SmsApiException("SMS API server error", true);
        } catch (ResourceAccessException e) {
            log.error("Network/Timeout error: {}", e.getMessage());
            throw new SmsApiException("SMS API connectivity error", true);
        } catch (InvalidMessageException e) {
            log.error("Validation failed for notification {}: {}", message.notificationId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error for {}: {}", message.notificationId(), e.getMessage());
            throw new SmsApiException("Internal worker error: " + e.getMessage(), false);
        }
    }

    private void sendViaSmsc(NotificationCommandDto message) {
        log.info("Sending SMS via SMSC.ru to {}", message.recipient());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("login", props.getApi().getLogin());
        body.add("psw", props.getApi().getPassword());
        body.add("phones", message.recipient());
        body.add("mes", message.text());
        body.add("fmt", "3");
        body.add("charset", "utf-8");
        if (props.getApi().isTestMode()) {
            body.add("test", "1");
            log.info("[TEST MODE] SMS will not actually be delivered");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://smsc.ru/sys/send.php",
                new HttpEntity<>(body, headers),
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SmsApiException("SMSC returned HTTP " + response.getStatusCode(), true);
        }

        String messageId = extractSmscMessageId(response.getBody(), message);
        log.info("SMS sent via SMSC.ru for notification {}. ID: {}", message.notificationId(), messageId);
        statusService.publishSent(message, messageId);
    }

    private void sendViaTextbelt(NotificationCommandDto message) {
        log.info("Sending SMS via TextBelt to {}", message.recipient());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("phone", message.recipient());
        body.add("message", message.text());
        body.add("key", props.getApi().getTextbeltKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://textbelt.com/text",
                new HttpEntity<>(body, headers),
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SmsApiException("TextBelt returned HTTP " + response.getStatusCode(), true);
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (!root.path("success").asBoolean(false)) {
                String error = root.path("error").asText("unknown error");
                throw new SmsApiException("TextBelt error: " + error, false);
            }
            String textId = root.path("textId").asText("unknown");
            log.info("SMS sent via TextBelt for notification {}. textId: {}", message.notificationId(), textId);
            statusService.publishSent(message, textId);
        } catch (SmsApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not parse TextBelt response: {}", e.getMessage());
            statusService.publishSent(message, "unknown");
        }
    }

    private void sendViaTwilio(NotificationCommandDto message) {
        log.info("Sending SMS via Twilio to {}", message.recipient());

        if (!isSet(props.getApi().getFromNumber())) {
            throw new InvalidMessageException("SMS FROM number is not configured");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("From", props.getApi().getFromNumber());
        body.add("To", message.recipient());
        body.add("Body", message.text());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", props.getApi().getToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.postForEntity(
                props.getApi().getBaseUrl(),
                new HttpEntity<>(body, headers),
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new SmsApiException("Twilio returned HTTP " + response.getStatusCode(),
                    response.getStatusCode().is5xxServerError());
        }

        String sid = extractTwilioSid(response.getBody());
        log.info("SMS sent via Twilio for notification {}. SID: {}", message.notificationId(), sid);
        statusService.publishSent(message, sid);
    }

    private void validateMessage(NotificationCommandDto message) {
        String phone = message.recipient();
        if (phone == null || phone.isBlank()) {
            throw new InvalidMessageException("Recipient phone is required");
        }
        if (!phone.startsWith("+")) {
            throw new InvalidMessageException("Phone number must be in E.164 format: " + phone);
        }
    }

    private String extractSmscMessageId(String responseBody, NotificationCommandDto message) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("error_code")) {
                int code = root.path("error_code").asInt();
                String error = root.path("error").asText("SMSC error");
                boolean retryable = (code == 3 || code == 9);
                throw new SmsApiException("SMSC error " + code + ": " + error, retryable);
            }
            return root.path("id").asText("unknown");
        } catch (SmsApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not parse SMSC response: {}", e.getMessage());
            return "unknown";
        }
    }

    private String extractTwilioSid(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("sid").asText("unknown");
        } catch (Exception e) {
            log.warn("Could not parse Twilio response: {}", e.getMessage());
            return "unknown";
        }
    }

    private boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}