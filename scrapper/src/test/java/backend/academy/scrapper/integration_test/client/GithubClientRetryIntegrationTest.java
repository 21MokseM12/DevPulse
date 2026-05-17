package backend.academy.scrapper.integration_test.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.config.ClientConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

class GithubClientRetryIntegrationTest {

    @Test
    void getEvents_whenFirstConnectionClosedPrematurely_retriesAndGetsResponse() throws Exception {
        try (PrematureCloseServer server = new PrematureCloseServer()) {
            ScrapperConfig scrapperConfig = new ScrapperConfig(
                    new ScrapperConfig.GitHubCredentials("github-token", "http://127.0.0.1:" + server.port()),
                    new ScrapperConfig.StackOverflowCredentials(
                            "https://api.stackexchange.com/2.3", "so-key", "so-access-token"),
                    "http://localhost:8080",
                    new ScrapperConfig.SchedulerCredentials(Duration.ofSeconds(15), Duration.ofSeconds(30), 4),
                    new ScrapperConfig.OutboxCredentials("link-updates", Duration.ofSeconds(5), 100),
                    new ScrapperConfig.DeliveryCredentials(ScrapperConfig.DeliveryMode.HTTP),
                    new ScrapperConfig.AuthCredentials("X-Internal-Secret", "secret"));
            ClientConfig clientConfig = new ClientConfig(scrapperConfig);

            GithubClient githubClient =
                    clientConfig.githubClient(RestClient.builder(), clientConfig.externalApiRequestFactory());
            ExternalApiResilienceExecutor executor = new ExternalApiResilienceExecutor(
                    RetryRegistry.of(RetryConfig.custom()
                            .maxAttempts(3)
                            .waitDuration(Duration.ofMillis(50))
                            .retryExceptions(ResourceAccessException.class)
                            .build()),
                    CircuitBreakerRegistry.of(CircuitBreakerConfig.ofDefaults()),
                    RateLimiterRegistry.of(RateLimiterConfig.custom()
                            .limitForPeriod(10)
                            .limitRefreshPeriod(Duration.ofSeconds(1))
                            .timeoutDuration(Duration.ZERO)
                            .build()));

            ResponseEntity<List<GithubResponse>> response =
                    executor.execute("github-api", () -> githubClient.getEvents("acme", "repo", null, null));

            assertNotNull(response);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
            assertEquals(2, server.acceptedConnections());
        }
    }

    private static final class PrematureCloseServer implements AutoCloseable {
        private static final int REQUEST_SOCKET_TIMEOUT_MILLIS = 2_000;

        private final ServerSocket serverSocket;
        private final ExecutorService executorService;
        private final AtomicInteger acceptedConnections;

        private PrematureCloseServer() throws Exception {
            this.serverSocket = new ServerSocket(0);
            this.executorService = Executors.newSingleThreadExecutor();
            this.acceptedConnections = new AtomicInteger();
            executorService.submit(this::serve);
        }

        private void serve() {
            while (!serverSocket.isClosed() && acceptedConnections.get() < 2) {
                try (Socket socket = serverSocket.accept()) {
                    int currentAttempt = acceptedConnections.incrementAndGet();
                    if (currentAttempt == 1) {
                        continue;
                    }

                    socket.setSoTimeout(REQUEST_SOCKET_TIMEOUT_MILLIS);
                    readRequestHeaders(socket);
                    writeSuccessfulJsonResponse(socket.getOutputStream());
                } catch (SocketException socketException) {
                    if (serverSocket.isClosed()) {
                        return;
                    }
                    throw new IllegalStateException("Socket error while serving test request", socketException);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to serve test request", ex);
                }
            }
        }

        private void readRequestHeaders(Socket socket) throws Exception {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            String line;
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                // Drain request headers before sending response.
            }
        }

        private void writeSuccessfulJsonResponse(OutputStream outputStream) throws Exception {
            byte[] body = "[]".getBytes(StandardCharsets.UTF_8);
            String headers = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: "
                    + body.length
                    + "\r\n"
                    + "Connection: close\r\n"
                    + "\r\n";
            outputStream.write(headers.getBytes(StandardCharsets.US_ASCII));
            outputStream.write(body);
            outputStream.flush();
        }

        private int port() {
            return serverSocket.getLocalPort();
        }

        private int acceptedConnections() {
            return acceptedConnections.get();
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
