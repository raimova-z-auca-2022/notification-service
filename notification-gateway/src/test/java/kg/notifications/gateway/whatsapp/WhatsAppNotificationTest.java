package kg.notifications.gateway.whatsapp;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
@Slf4j
public class WhatsAppNotificationTest {

    @Test
    void testWhatsAppBroadcast() {

        RestAssured.baseURI = "http://localhost:8080";
        String requestId = UUID.randomUUID().toString();

        log.info(" Запуск теста: Отправка WhatsApp уведомления");
        log.info(" Idempotency-Key: {}", requestId);


        String requestBody = """
            {
                "type": "WHATSAPP",
                "text": "Привет! Это сообщение от первого автотеста Даниэла.",
                "recipients": ["+996500130605"]
            }
            """;


        given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .body(requestBody)
                .when()
                .post("/notifications/broadcast")
                .then()
                .statusCode(200)
                .body(containsString("успешно"));
        log.info(" Тест завершился успешно! Гейтвей принял запрос.");
    }
}