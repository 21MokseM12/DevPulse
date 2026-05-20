package backend.academy.bot.service.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.config.properties.PushProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class FcmHttpPushSenderTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer fcmServer;

    @AfterEach
    void tearDown() {
        if (fcmServer != null) {
            fcmServer.stop(0);
        }
    }

    @Test
    void send_postsV1PayloadWithBearerToken() throws Exception {
        CapturedRequest capturedRequest = new CapturedRequest();
        fcmServer = HttpServer.create(new InetSocketAddress(0), 0);
        fcmServer.createContext("/v1/projects/test-project/messages:send", exchange -> {
            capture(exchange, capturedRequest);
            respond(exchange, 200, "{\"name\":\"projects/test/messages/1\"}");
        });
        fcmServer.start();

        FcmAccessTokenProvider tokenProvider = mock(FcmAccessTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("oauth-token");
        FcmHttpPushSender sender =
                createSender(tokenProvider, fcmServer.getAddress().getPort());

        PushDeliveryResult result = sender.send(
                "device-token-1",
                new PushMessagePayload(
                        "1001",
                        "title",
                        "content",
                        "https://example.com/1",
                        "2026-05-20T12:00:00Z",
                        "octocat",
                        Map.of("event_id", "1001", "url", "https://example.com/1")));

        assertThat(result.status()).isEqualTo(PushDeliveryStatus.SUCCESS);
        assertThat(capturedRequest.authorization).isEqualTo("Bearer oauth-token");
        Map<?, ?> requestBody = objectMapper.readValue(capturedRequest.body, Map.class);
        assertThat(((Map<?, ?>) requestBody.get("message")).get("token")).isEqualTo("device-token-1");
        assertThat(((Map<?, ?>) ((Map<?, ?>) requestBody.get("message")).get("android")).get("priority"))
                .isEqualTo("HIGH");
        assertThat(((Map<?, ?>) requestBody.get("message")).get("data"))
                .isEqualTo(Map.of(
                        "event_id", "1001",
                        "url", "https://example.com/1"));
    }

    @Test
    void send_mapsUnregisteredToInvalidToken() throws Exception {
        fcmServer = HttpServer.create(new InetSocketAddress(0), 0);
        fcmServer.createContext(
                "/v1/projects/test-project/messages:send",
                exchange -> respond(
                        exchange,
                        404,
                        """
                {"error":{"code":404,"message":"Requested entity was not found.","status":"NOT_FOUND","details":[{"@type":"type.googleapis.com/google.firebase.fcm.v1.FcmError","errorCode":"UNREGISTERED"}]}}
                """));
        fcmServer.start();

        FcmAccessTokenProvider tokenProvider = mock(FcmAccessTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("oauth-token");
        FcmHttpPushSender sender =
                createSender(tokenProvider, fcmServer.getAddress().getPort());

        PushDeliveryResult result = sender.send("device-token-1", minimalPayload());

        assertThat(result.status()).isEqualTo(PushDeliveryStatus.INVALID_TOKEN);
        assertThat(result.reason()).isEqualTo("UNREGISTERED");
    }

    @Test
    void send_mapsQuotaExceededToTransientFailure() throws Exception {
        fcmServer = HttpServer.create(new InetSocketAddress(0), 0);
        fcmServer.createContext(
                "/v1/projects/test-project/messages:send",
                exchange -> respond(
                        exchange,
                        429,
                        """
                {"error":{"code":429,"message":"quota exceeded","status":"QUOTA_EXCEEDED","details":[]}}
                """));
        fcmServer.start();

        FcmAccessTokenProvider tokenProvider = mock(FcmAccessTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("oauth-token");
        FcmHttpPushSender sender =
                createSender(tokenProvider, fcmServer.getAddress().getPort());

        PushDeliveryResult result = sender.send("device-token-1", minimalPayload());

        assertThat(result.status()).isEqualTo(PushDeliveryStatus.TRANSIENT_ERROR);
        assertThat(result.reason()).isEqualTo("QUOTA_EXCEEDED");
    }

    @Test
    void send_mapsPermissionDeniedToPermanentFailure() throws Exception {
        fcmServer = HttpServer.create(new InetSocketAddress(0), 0);
        fcmServer.createContext(
                "/v1/projects/test-project/messages:send",
                exchange -> respond(
                        exchange,
                        403,
                        """
                {"error":{"code":403,"message":"permission denied","status":"PERMISSION_DENIED","details":[]}}
                """));
        fcmServer.start();

        FcmAccessTokenProvider tokenProvider = mock(FcmAccessTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("oauth-token");
        FcmHttpPushSender sender =
                createSender(tokenProvider, fcmServer.getAddress().getPort());

        PushDeliveryResult result = sender.send("device-token-1", minimalPayload());

        assertThat(result.status()).isEqualTo(PushDeliveryStatus.PERMANENT_ERROR);
        assertThat(result.reason()).isEqualTo("PERMISSION_DENIED");
    }

    private FcmHttpPushSender createSender(FcmAccessTokenProvider tokenProvider, int port) {
        PushProperties properties = new PushProperties(
                new PushProperties.FcmProperties(
                        "test-project", "", "http://localhost:%d/v1/projects/%%s/messages:send".formatted(port)),
                new PushProperties.RetryProperties(3, 1000, 2.0, 5000),
                60);
        return new FcmHttpPushSender(properties, WebClient.builder(), objectMapper, tokenProvider);
    }

    private PushMessagePayload minimalPayload() {
        return new PushMessagePayload(
                "1001",
                "title",
                "content",
                "https://example.com/1",
                "2026-05-20T12:00:00Z",
                "octocat",
                Map.of("event_id", "1001"));
    }

    private void capture(HttpExchange exchange, CapturedRequest capturedRequest) {
        try {
            capturedRequest.authorization = exchange.getRequestHeaders().getFirst("Authorization");
            capturedRequest.body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to capture request", ex);
        }
    }

    private void respond(HttpExchange exchange, int status, String body) {
        try {
            byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send response", ex);
        }
    }

    private static final class CapturedRequest {
        private String authorization;
        private String body;
    }
}
