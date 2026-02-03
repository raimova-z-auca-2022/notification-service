package kg.notifications.gateway.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("NotificationController Integration Tests")
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /stats endpoint exists")
    @Disabled("Requires database setup")
    public void testGetStats() throws Exception {
        mockMvc.perform(get("/api/v1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSent").exists())
                .andExpect(jsonPath("$.pending").exists())
                .andExpect(jsonPath("$.scheduled").exists());
    }

    @Test
    @DisplayName("POST /api/telegram/generate-link endpoint exists")
    @Disabled("Requires RabbitMQ setup")
    public void testGenerateTelegramLink() throws Exception {
        mockMvc.perform(post("/api/telegram/generate-link")
                .param("userId", "test-user-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return proper error responses")
    public void testErrorHandling() throws Exception {
        mockMvc.perform(get("/api/v1/stats"))
                .andExpect(status().is5xxServerError());
    }
}



