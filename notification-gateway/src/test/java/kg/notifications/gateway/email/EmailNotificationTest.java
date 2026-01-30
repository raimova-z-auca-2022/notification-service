package kg.notifications.gateway.email;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@Slf4j
public class EmailNotificationTest {

    @Test
    void testEmailSend() {
        RestAssured.baseURI = "http://localhost:8080";
        String requestId = UUID.randomUUID().toString();

        log.info("📧 Запуск теста отправки Email");


        String body = """
            {
                "type": "EMAIL",
                "text": "Привет, Даниэл! Email Worker работает!",
                "recipients": ["danchousss13@gmail.com"]
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", requestId)
                .body(body)
                .when()
                .post("/notifications/broadcast")
                .then()
                .statusCode(200)
                .body(containsString("успешно"));

        log.info(" Запрос на Email отправлен в Гейтвей!");
    }
}