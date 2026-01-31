package kg.notifications.whatsapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kg.notifications.whatsapp.config.AppProperties;
import kg.notifications.whatsapp.dto.NotificationCommandDto; // Используем новый DTO
import kg.notifications.whatsapp.exception.InvalidMessageException;
import kg.notifications.whatsapp.exception.WhatsAppApiException;
import kg.notifications.whatsapp.listener.StatusEventPublisher;
import kg.notifications.whatsapp.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppServiceImpl implements WhatsAppService {

    private final RestTemplate restTemplate;
    private final AppProperties props;
    private final ObjectMapper objectMapper;
    private final StatusEventPublisher statusService;

    @Override
    public void sendMessage(NotificationCommandDto message) {
        log.debug("Starting WhatsApp send process for notification: {}", message.notificationId());

        try {
            validateMessage(message);

            HttpHeaders headers = createHeaders();
            MultiValueMap<String, String> body = createRequestBody(message);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            log.info("Sending WhatsApp message via Twilio to {}", message.recipient());
            ResponseEntity<String> response = restTemplate.postForEntity(
                    props.getApi().getBaseUrl(),
                    request,
                    String.class
            );

            handleResponse(message, response);

        } catch (HttpClientErrorException e) {
            handleClientError(message, e);
        } catch (HttpServerErrorException e) {
            handleServerError(message, e);
        } catch (ResourceAccessException e) {
            handleNetworkError(message, e);
        } catch (InvalidMessageException e) {
            log.error("Validation failed for notification {}: {}", message.notificationId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleUnexpectedError(message, e);
        }
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

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", props.getApi().getToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private MultiValueMap<String, String> createRequestBody(NotificationCommandDto message) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        String fromNumber = props.getApi().getFromNumber();
        if (fromNumber == null || fromNumber.isBlank()) {
            throw new InvalidMessageException("WhatsApp FROM number is not configured in application.yml");
        }

        body.add("From", fromNumber);
        body.add("To", ensureWhatsappPrefix(message.recipient()));
        // В рекорде используем .text() вместо .getText()
        body.add("Body", message.text());
        return body;
    }

    private void handleResponse(NotificationCommandDto message, ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            String sid = extractMessageId(response.getBody());
            log.info("Successfully sent notification {} to Twilio. SID: {}", message.notificationId(), sid);

            // Здесь statusService тоже должен принимать NotificationCommandDto
            statusService.publishSent(message, sid);
        } else {
            throw new WhatsAppApiException(
                    "Twilio API returned non-success: " + response.getStatusCode(),
                    response.getStatusCode().is5xxServerError()
            );
        }
    }

    // Методы обработки ошибок (Client/Server/Network) также нужно обновить,
    // чтобы они принимали NotificationCommandDto в аргументах
    private void handleClientError(NotificationCommandDto message, HttpClientErrorException e) {
        String body = e.getResponseBodyAsString();
        log.error("Twilio Client Error ({}): {}", e.getStatusCode(), body);
        boolean retryable = (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS);
        throw new WhatsAppApiException("Twilio client error: " + e.getStatusCode(), retryable);
    }

    private void handleServerError(NotificationCommandDto message, HttpServerErrorException e) {
        log.error("Twilio Server Error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
        throw new WhatsAppApiException("Twilio server error", true);
    }

    private void handleNetworkError(NotificationCommandDto message, ResourceAccessException e) {
        log.error("Network/Timeout error: {}", e.getMessage());
        throw new WhatsAppApiException("Twilio connectivity error", true);
    }

    private void handleUnexpectedError(NotificationCommandDto message, Exception e) {
        log.error("Unexpected error for {}: {}", message.notificationId(), e.getMessage());
        throw new WhatsAppApiException("Internal worker error: " + e.getMessage(), false);
    }

    private String extractMessageId(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("sid").asText("unknown");
        } catch (Exception e) {
            log.warn("Could not parse Twilio response JSON: {}", e.getMessage());
            return "unknown";
        }
    }

    private String ensureWhatsappPrefix(String number) {
        String cleaned = number.trim();
        if (cleaned.startsWith("whatsapp:")) return cleaned;
        return "whatsapp:" + (cleaned.startsWith("+") ? cleaned : "+" + cleaned);
    }
}