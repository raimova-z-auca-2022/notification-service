package kg.notifications.gateway.broadcast;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
public class BroadcastNotificationTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    @DisplayName("Тестирование массовой рассылки (Broadcast)")
    void testMultipleRecipients() {
        String requestId = UUID.randomUUID().toString();

        log.info(" Запуск теста массовой рассылки");
        log.info(" Request ID: {}", requestId);

        // Список получателей (можешь добавить свои номера)
        List<String> recipients = List.of("+996500130605", "+996500030467");

        String body = String.format("""
            {
                "type": "WHATSAPP",
                "text": "Тест массовой рассылки запущен успешно!",
                "recipients": ["%s", "%s"]
            }
            """, recipients.get(0), recipients.get(1));

        given()
                .log().all() // Логируем весь исходящий запрос
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", requestId)
                .body(body)
                .when()
                .post("/notifications/broadcast")
                .then()
                .log().all() // Логируем весь ответ от сервера (теперь ты увидишь JSON ответа!)
                .statusCode(200)
                .body(containsString("успешно"));

        log.info(" Массовая рассылка принята Гейтвеем!");
    }
}