package kg.notifications.whatsapp.service.impl;

import kg.notifications.whatsapp.config.AppProperties;
import kg.notifications.whatsapp.dto.WhatsAppApiResponse;
import kg.notifications.whatsapp.dto.WhatsAppNotificationMessage;
import kg.notifications.whatsapp.exception.InvalidMessageException;
import kg.notifications.whatsapp.exception.WhatsAppApiException;
import kg.notifications.whatsapp.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppServiceImpl implements WhatsAppService {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    @Override
    public void sendMessage(WhatsAppNotificationMessage message) {
        validateMessage(message);

        try {
            log.info("Sending WhatsApp message to {}: {}",
                    message.getMaskedRecipient(),
                    message.getNotificationId());

            HttpHeaders headers = createHeaders();
            Object requestBody = createRequestBody(message);
            HttpEntity<Object> request = new HttpEntity<>(requestBody, headers);

            String apiUrl = appProperties.getApi().getBaseUrl() + "/messages";

            ResponseEntity<WhatsAppApiResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    WhatsAppApiResponse.class
            );

            handleResponse(message, response);

        } catch (HttpClientErrorException e) {
            handleClientError(message, e);
        } catch (HttpServerErrorException e) {
            handleServerError(message, e);
        } catch (ResourceAccessException e) {
            handleNetworkError(message, e);
        } catch (Exception e) {
            handleUnexpectedError(message, e);
        }
    }

    private void validateMessage(WhatsAppNotificationMessage message) {
        if (!message.isValid()) {
            throw new InvalidMessageException(
                    "Invalid WhatsApp message: " + message.getValidationErrors());
        }

        String phone = message.getRecipient();
        if (!phone.startsWith("+")) {
            throw new InvalidMessageException(
                    "Phone number must be in E.164 format (start with +): " + phone);
        }

        if (phone.length() < 10 || phone.length() > 15) {
            throw new InvalidMessageException(
                    "Phone number length invalid: " + phone.length());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(appProperties.getApi().getToken());
        return headers;
    }

    private Object createRequestBody(WhatsAppNotificationMessage message) {
        if (message.getTemplateId() != null) {
            return createTemplateRequestBody(message);
        } else {
            return createTextRequestBody(message);
        }
    }

    private Map<String, Object> createTextRequestBody(WhatsAppNotificationMessage message) {
        return Map.of(
                "to", message.getRecipient(),
                "type", "text",
                "text", Map.of(
                        "body", message.getMessage(),
                        "preview_url", false
                )
        );
    }

    private Map<String, Object> createTemplateRequestBody(WhatsAppNotificationMessage message) {
        Map<String, Object> template = Map.of(
                "name", message.getTemplateId(),
                "language", Map.of("code", "ru"),
                "components", createTemplateComponents(message)
        );

        return Map.of(
                "to", message.getRecipient(),
                "type", "template",
                "template", template
        );
    }

    private List<Map<String, Object>> createTemplateComponents(WhatsAppNotificationMessage message) {
        if (message.getTemplateVariables() == null ||
                message.getTemplateVariables().isEmpty()) {
            return Collections.emptyList();
        }

        return List.of(
                Map.of(
                        "type", "body",
                        "parameters", message.getTemplateVariables().entrySet().stream()
                                .map(entry -> Map.of(
                                        "type", "text",
                                        "text", entry.getValue()
                                ))
                                .toList()
                )
        );
    }

    private void handleResponse(WhatsAppNotificationMessage message,
                                ResponseEntity<WhatsAppApiResponse> response) {
        WhatsAppApiResponse apiResponse = response.getBody();

        if (response.getStatusCode().is2xxSuccessful() &&
                apiResponse != null &&
                apiResponse.isSuccess()) {

            log.info("WhatsApp message sent successfully: {} [Message ID: {}]",
                    message.getNotificationId(),
                    apiResponse.getMessageId());

        } else {
            String errorMsg = apiResponse != null ?
                    apiResponse.getErrorMessage() : "Unknown API error";
            throw new WhatsAppApiException(
                    "WhatsApp API returned error: " + errorMsg,
                    false
            );
        }
    }

    private void handleClientError(WhatsAppNotificationMessage message,
                                   HttpClientErrorException e) {
        log.error("WhatsApp API client error ({}): {} for notification {}",
                e.getStatusCode(),
                e.getMessage(),
                message.getNotificationId());
        throw new WhatsAppApiException(
                "Client error: " + e.getStatusCode() + " - " + e.getMessage(),
                false
        );
    }

    private void handleServerError(WhatsAppNotificationMessage message,
                                   HttpServerErrorException e) {
        log.error("WhatsApp API server error ({}): {} for notification {}",
                e.getStatusCode(),
                e.getMessage(),
                message.getNotificationId());
        throw new WhatsAppApiException(
                "Server error: " + e.getStatusCode() + " - " + e.getMessage(),
                true
        );
    }

    private void handleNetworkError(WhatsAppNotificationMessage message,
                                    ResourceAccessException e) {
        log.error("WhatsApp API network/timeout error: {} for notification {}",
                e.getMessage(),
                message.getNotificationId());
        throw new WhatsAppApiException(
                "Network/timeout error: " + e.getMessage(),
                true
        );
    }

    private void handleUnexpectedError(WhatsAppNotificationMessage message,
                                       Exception e) {
        log.error("Unexpected error sending WhatsApp message {}: {}",
                message.getNotificationId(),
                e.getMessage(),
                e);
        throw new WhatsAppApiException(
                "Unexpected error: " + e.getMessage(),
                false
        );
    }
}