package kg.notifications.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppApiResponse {
    private boolean success;
    private String messageId;
    private String status;
    private String errorCode;
    private String errorMessage;
    private Map<String, Object> metadata;
}