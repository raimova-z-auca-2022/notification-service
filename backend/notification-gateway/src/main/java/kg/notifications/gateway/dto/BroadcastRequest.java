package kg.notifications.gateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor // Обязательно для JSON (Jackson)
@AllArgsConstructor
public class BroadcastRequest {

    // Тип уведомления (WHATSAPP, EMAIL и т.д.)
    private NotificationType type;

    // Текст сообщения
    private String text;

    // Список получателей (массив номеров)
    private List<String> recipients;
}