package kg.notifications.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiBroadcastRequest {
    private List<NotificationType> types;
    private String text;
    private List<String> recipients;
}