package backend.academy.scrapper.integration_test.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.config.ClientConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class StackOverflowClientAuthIntegrationTest {

    private static final String APP_KEY = "so-app-key";
    private static final String ACCESS_TOKEN = "so-access-token";

    private WireMockServer wireMockServer;

    @BeforeEach
    void startServer() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        wireMockServer.stubFor(get(urlPathEqualTo("/questions/123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"items\":[]}")));
    }

    @AfterEach
    void stopServer() {
        wireMockServer.stop();
    }

    @Test
    void getQuestionById_whenKeyAndAccessTokenConfigured_appendsBothQueryParams() {
        StackOverflowClient client = buildClient(APP_KEY, ACCESS_TOKEN);

        client.getQuestionById(123L, "stackoverflow", null);

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/questions/123"))
                .withQueryParam("key", equalTo(APP_KEY))
                .withQueryParam("access_token", equalTo(ACCESS_TOKEN))
                .withQueryParam("site", equalTo("stackoverflow")));
    }

    @Test
    void getQuestionById_whenAccessTokenBlank_appendsOnlyKey() {
        StackOverflowClient client = buildClient(APP_KEY, "");

        client.getQuestionById(123L, "stackoverflow", null);

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/questions/123"))
                .withQueryParam("key", equalTo(APP_KEY))
                .withQueryParam("access_token", absent())
                .withQueryParam("site", equalTo("stackoverflow")));
    }

    private StackOverflowClient buildClient(String key, String accessToken) {
        ScrapperConfig scrapperConfig = new ScrapperConfig(
                new ScrapperConfig.GitHubCredentials("github-token", "https://api.github.com"),
                new ScrapperConfig.StackOverflowCredentials(wireMockServer.baseUrl(), key, accessToken),
                "http://localhost:8080",
                new ScrapperConfig.SchedulerCredentials(Duration.ofSeconds(15), Duration.ofSeconds(30), 4),
                new ScrapperConfig.OutboxCredentials("link-updates", Duration.ofSeconds(5), 100),
                new ScrapperConfig.DeliveryCredentials(ScrapperConfig.DeliveryMode.HTTP),
                new ScrapperConfig.AuthCredentials("X-Internal-Secret", "secret"));
        ClientConfig clientConfig = new ClientConfig(scrapperConfig);
        return clientConfig.stackOverflowClient(RestClient.builder(), clientConfig.externalApiRequestFactory());
    }
}
