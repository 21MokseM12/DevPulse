package backend.academy.bot.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.service.push.FcmAccessTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
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
    private static final HttpServer FCM_SERVER = createFcmServer();
    private static final String FCM_ENDPOINT_TEMPLATE = "http://localhost:%d/v1/projects/%%s/messages:send"
            .formatted(FCM_SERVER.getAddress().getPort());

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
        registry.add("app.push.fcm.project-id", () -> "integration-project");
        registry.add("app.push.fcm.endpoint-template", () -> FCM_ENDPOINT_TEMPLATE);
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
    private FcmAccessTokenProvider accessTokenProvider;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM push_tokens");
        jdbcTemplate.update("DELETE FROM notification_recipients");
        jdbcTemplate.update("DELETE FROM notifications");
        jdbcTemplate.update("DELETE FROM clients");
        when(chatClient.registerChat(any())).thenReturn(ResponseEntity.ok().build());
        when(chatClient.unregisterChat(any())).thenReturn(ResponseEntity.ok().build());
        when(accessTokenProvider.getAccessToken()).thenReturn("integration-token");
    }

    @AfterAll
    static void stopFcmServer() {
        FCM_SERVER.stop(0);
    }

    @Test
    void updates_sendPushAndInvalidateTokenOnUnregisteredResponse() throws Exception {
        registerClient("push-user");
        registerPushToken("push-user", "fcm-token-1111222233334444");

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

    private static HttpServer createFcmServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/v1/projects/integration-project/messages:send", exchange -> {
                String body =
                        """
                        {"error":{"code":404,"message":"Requested entity was not found.","status":"NOT_FOUND","details":[{"@type":"type.googleapis.com/google.firebase.fcm.v1.FcmError","errorCode":"UNREGISTERED"}]}}
                        """;
                byte[] response = body.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            server.start();
            return server;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize test FCM server", ex);
        }
    }
}
