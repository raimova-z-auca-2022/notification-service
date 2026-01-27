package kg.notifications.gateway.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusEventDto implements Serializable {
    private String notificationId;
    private String type;
    private String status;
    private int attempt;
    private String errorCode;
    private String errorMessage;
    private String providerMessageId;
    private OffsetDateTime occurredAt;
}