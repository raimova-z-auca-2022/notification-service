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

        try {
            validateMessage(message);

            HttpHeaders headers = createHeaders();
            MultiValueMap<String, String> body = createRequestBody(message);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            log.info("Sending SMS via provider to {}", message.recipient());
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
            throw new InvalidMessageException("SMS FROM number is not configured");
        }

        body.add("From", fromNumber);
        body.add("To", message.recipient());
        body.add("Body", message.text());
        return body;
    }

    private void handleResponse(NotificationCommandDto message, ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            String sid = extractMessageId(response.getBody());
            log.info("Successfully sent SMS notification {}. SID: {}", message.notificationId(), sid);
            statusService.publishSent(message, sid);
        } else {
            throw new SmsApiException(
                    "SMS API returned non-success: " + response.getStatusCode(),
                    response.getStatusCode().is5xxServerError()
            );
        }
    }

    private void handleClientError(NotificationCommandDto message, HttpClientErrorException e) {
        log.error("SMS API Client Error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
        boolean retryable = (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS);
        throw new SmsApiException("SMS API client error: " + e.getStatusCode(), retryable);
    }

    private void handleServerError(NotificationCommandDto message, HttpServerErrorException e) {
        log.error("SMS API Server Error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
        throw new SmsApiException("SMS API server error", true);
    }

    private void handleNetworkError(NotificationCommandDto message, ResourceAccessException e) {
        log.error("Network/Timeout error: {}", e.getMessage());
        throw new SmsApiException("SMS API connectivity error", true);
    }

    private void handleUnexpectedError(NotificationCommandDto message, Exception e) {
        log.error("Unexpected error for {}: {}", message.notificationId(), e.getMessage());
        throw new SmsApiException("Internal worker error: " + e.getMessage(), false);
    }

    private String extractMessageId(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("sid").asText("unknown");
        } catch (Exception e) {
            log.warn("Could not parse SMS API response JSON: {}", e.getMessage());
            return "unknown";
        }
    }
}