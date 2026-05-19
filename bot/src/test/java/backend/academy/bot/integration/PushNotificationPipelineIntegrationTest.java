package backend.academy.bot.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.service.push.PushDeliveryResult;
import backend.academy.bot.service.push.PushSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class PushNotificationPipelineIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("devpulse_bot_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.scrapper-url", () -> "http://localhost:9999");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatClient chatClient;

    @MockitoBean
    private PushSender pushSender;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM push_tokens");
        jdbcTemplate.update("DELETE FROM notification_recipients");
        jdbcTemplate.update("DELETE FROM notifications");
        jdbcTemplate.update("DELETE FROM clients");
        when(chatClient.registerChat(any())).thenReturn(ResponseEntity.ok().build());
        when(chatClient.unregisterChat(any())).thenReturn(ResponseEntity.ok().build());
    }

    @Test
    void updates_sendPushAndInvalidateTokenOnUnregisteredResponse() throws Exception {
        registerClient("push-user");
        registerPushToken("push-user", "fcm-token-1111222233334444");

        when(pushSender.send(eq("fcm-token-1111222233334444"), any()))
                .thenReturn(PushDeliveryResult.invalidToken("UNREGISTERED"));

        String updatePayload = objectMapper.writeValueAsString(Map.of(
                "id", 3001,
                "url", "https://github.com/org/repo/issues/3001",
                "title", "Issue updated",
                "updateOwner", "octocat",
                "description", "Details",
                "creationDate", OffsetDateTime.parse("2026-05-01T00:00:00Z"),
                "clientLogins", List.of("push-user")));
        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "test-internal-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());

        Awaitility.await().untilAsserted(() -> {
            String tokenStatus = jdbcTemplate.queryForObject(
                    "SELECT status FROM push_tokens WHERE client_login = 'push-user' AND token = 'fcm-token-1111222233334444'",
                    String.class);
            assertThat(tokenStatus).isEqualTo("invalid");
        });
    }

    private void registerClient(String login) {
        jdbcTemplate.update("INSERT INTO clients(login, password_hash) VALUES (?, ?)", login, "hash");
    }

    private void registerPushToken(String login, String token) {
        jdbcTemplate.update(
                "INSERT INTO push_tokens(client_login, platform, token, status) VALUES (?, 'android', ?, 'active')",
                login,
                token);
    }
}
