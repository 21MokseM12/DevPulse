package backend.academy.bot.service.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.bot.config.properties.PushProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class ServiceAccountFcmAccessTokenProviderTest {
    private HttpServer tokenServer;

    @AfterEach
    void tearDown() {
        if (tokenServer != null) {
            tokenServer.stop(0);
        }
    }

    @Test
    void getAccessToken_cachesTokenAndRefreshesNearExpiry() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        tokenServer = HttpServer.create(new InetSocketAddress(0), 0);
        tokenServer.createContext("/oauth/token", exchange -> {
            calls.incrementAndGet();
            String body = calls.get() == 1
                    ? "{\"access_token\":\"token-1\",\"expires_in\":3600}"
                    : "{\"access_token\":\"token-2\",\"expires_in\":3600}";
            byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        tokenServer.start();

        String tokenUri = "http://localhost:" + tokenServer.getAddress().getPort() + "/oauth/token";
        Path credentials = createCredentialsFile(tokenUri);
        MutableClock clock = new MutableClock(Instant.parse("2026-05-20T10:00:00Z"));
        ServiceAccountFcmAccessTokenProvider provider = createProvider(credentials.toString(), "test-project", clock);

        String first = provider.getAccessToken();
        String second = provider.getAccessToken();
        clock.advanceSeconds(3500);
        String third = provider.getAccessToken();
        clock.advanceSeconds(100);
        String fourth = provider.getAccessToken();

        assertThat(first).isEqualTo("token-1");
        assertThat(second).isEqualTo("token-1");
        assertThat(third).isEqualTo("token-1");
        assertThat(fourth).isEqualTo("token-2");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void getAccessToken_throwsWhenCredentialsFileIsInvalid() {
        ServiceAccountFcmAccessTokenProvider provider =
                createProvider("/path/that/does/not/exist.json", "test-project", new MutableClock(Instant.now()));

        assertThatThrownBy(provider::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to read service account JSON");
    }

    private ServiceAccountFcmAccessTokenProvider createProvider(String credentialsPath, String projectId, Clock clock) {
        PushProperties properties = new PushProperties(
                new PushProperties.FcmProperties(
                        projectId, credentialsPath, "http://localhost/v1/projects/%s/messages:send"),
                new PushProperties.RetryProperties(3, 1000, 2.0, 5000),
                60);
        return new ServiceAccountFcmAccessTokenProvider(properties, WebClient.builder(), new ObjectMapper(), clock);
    }

    private Path createCredentialsFile(String tokenUri) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                        .encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
        Path file = Files.createTempFile("service-account", ".json");
        String credentialsJson =
                """
                {
                  "client_email": "devpulse-test@project.iam.gserviceaccount.com",
                  "private_key": "%s",
                  "token_uri": "%s"
                }
                """
                        .formatted(pem.replace("\n", "\\n"), tokenUri);
        Files.writeString(file, credentialsJson, StandardCharsets.UTF_8);
        return file;
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }

        private void advanceSeconds(long seconds) {
            now = now.plusSeconds(seconds);
        }
    }
}
