package backend.academy.bot.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class BotHttpLinkFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("devpulse_bot_http_test")
            .withUsername("test")
            .withPassword("test");

    private static final HttpServer SCRAPPER_STUB_SERVER = createStubServer();
    private static final AtomicInteger getStatus = new AtomicInteger(200);
    private static final AtomicInteger postStatus = new AtomicInteger(200);
    private static final AtomicInteger deleteStatus = new AtomicInteger(200);
    private static final AtomicReference<String> getBody = new AtomicReference<>("[]");
    private static final AtomicReference<String> postBody = new AtomicReference<>("null");
    private static final AtomicReference<String> deleteBody = new AtomicReference<>("null");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add(
                "app.scrapper-url",
                () -> "http://localhost:" + SCRAPPER_STUB_SERVER.getAddress().getPort());
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetStub() {
        getStatus.set(200);
        postStatus.set(200);
        deleteStatus.set(200);
        getBody.set("[]");
        postBody.set(linkResponseJson(1001L, "https://github.com/org/repo/issues/1"));
        deleteBody.set(linkResponseJson(1001L, "https://github.com/org/repo/issues/1"));
    }

    @AfterAll
    static void shutdownServer() {
        SCRAPPER_STUB_SERVER.stop(0);
    }

    @Test
    void httpMode_subscribeAndUnsubscribeLink_happyPath() throws Exception {
        String link = "https://github.com/org/repo/issues/1";
        getBody.set("[" + linkResponseJson(1001L, link) + "]");
        postBody.set(linkResponseJson(1001L, link));
        deleteBody.set(linkResponseJson(1001L, link));

        mockMvc.perform(post("/api/v1/links")
                        .header("Client-Login", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"" + link + "\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/links")
                        .header("Client-Login", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"" + link + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void httpMode_scrapper400And404_areMappedToClientErrors() throws Exception {
        postStatus.set(400);
        postBody.set(apiErrorJson("Bad request", "400", "BadRequestException", "invalid"));

        mockMvc.perform(post("/api/v1/links")
                        .header("Client-Login", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/org/repo/issues/2\"}"))
                .andExpect(status().isBadRequest());

        getStatus.set(404);
        getBody.set(apiErrorJson("Resource not found", "404", "ResourceNotFoundException", "not found"));

        mockMvc.perform(get("/api/v1/links").header("Client-Login", "alice")).andExpect(status().isBadRequest());
    }

    @Test
    void notificationsFilterByTags_returnsOnlyMatchingLinks() throws Exception {
        registerClient("1", "1");
        getBody.set("["
                + linkResponseJsonWithTags(1001L, "https://github.com/org/repo/issues/1", "[\"backend\"]", "[]")
                + ","
                + linkResponseJsonWithTags(1002L, "https://github.com/org/repo/issues/2", "[\"mobile\"]", "[]")
                + "]");
        postInternalUpdate(updatePayload(1001L, "https://github.com/org/repo/issues/1", 1));
        postInternalUpdate(updatePayload(1002L, "https://github.com/org/repo/issues/2", 1));

        mockMvc.perform(get("/api/v1/notifications").header("Client-Login", "1").param("tags", "backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.length()").value(1))
                .andExpect(jsonPath("$.notifications[0].url").value("https://github.com/org/repo/issues/1"));
    }

    private static HttpServer createStubServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/links", new StubLinksHandler());
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start scrapper stub server", e);
        }
    }

    private static String linkResponseJson(long id, String url) {
        return "{" + "\"id\":" + id + "," + "\"url\":\"" + url + "\"," + "\"tags\":[]," + "\"filters\":[]" + "}";
    }

    private static String linkResponseJsonWithTags(long id, String url, String tagsJson, String filtersJson) {
        return "{"
                + "\"id\":"
                + id
                + ","
                + "\"url\":\""
                + url
                + "\","
                + "\"tags\":"
                + tagsJson
                + ","
                + "\"filters\":"
                + filtersJson
                + "}";
    }

    private static String apiErrorJson(String description, String code, String exceptionName, String message) {
        return "{"
                + "\"description\":\"" + description + "\","
                + "\"code\":\"" + code + "\","
                + "\"exceptionName\":\"" + exceptionName + "\","
                + "\"exceptionMessage\":\"" + message + "\","
                + "\"stacktrace\":[]"
                + "}";
    }

    private String updatePayload(long id, String url, long clientId) {
        return "{"
                + "\"id\":"
                + id
                + ","
                + "\"url\":\""
                + url
                + "\","
                + "\"title\":\"title\","
                + "\"updateOwner\":\"owner\","
                + "\"description\":\"description\","
                + "\"creationDate\":\"2026-04-26T00:00:00Z\","
                + "\"clientsIds\":["
                + clientId
                + "]"
                + "}";
    }

    private void registerClient(String login, String password) throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk());
    }

    private void postInternalUpdate(String payload) throws Exception {
        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "test-internal-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    private static class StubLinksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            int status;
            String body;

            if ("GET".equals(method)) {
                status = getStatus.get();
                body = getBody.get();
            } else if ("POST".equals(method)) {
                status = postStatus.get();
                body = postBody.get();
            } else if ("DELETE".equals(method)) {
                status = deleteStatus.get();
                body = deleteBody.get();
            } else {
                status = 405;
                body = "";
            }

            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
