package backend.academy.scrapper.config;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {

    private static final String BASE_GITHUB_URL = "https://api.github.com";

    private static final String BASE_BOT_URL = "http://localhost:8080";

    private static final String BASE_STACKOVERFLOW_URL = "https://api.stackexchange.com/2.3";

    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;

    private static final int READ_WRITE_TIMEOUT_SECONDS = 10;

    private final ScrapperConfig scrapperConfig;

    @Bean
    public GithubClient githubClient(
            RestClient.Builder builder, ReactorClientHttpRequestFactory externalApiRequestFactory) {
        RestClient restClient = builder.baseUrl(
                        scrapperConfig.github().url() == null
                                ? BASE_GITHUB_URL
                                : scrapperConfig.github().url())
                .requestFactory(externalApiRequestFactory)
                .defaultHeader(
                        "Authorization", "token " + scrapperConfig.github().token())
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(GithubClient.class);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(
            RestClient.Builder builder, ReactorClientHttpRequestFactory externalApiRequestFactory) {
        RestClient restClient = builder.baseUrl(
                        scrapperConfig.stackOverflow().url() == null
                                ? BASE_STACKOVERFLOW_URL
                                : scrapperConfig.stackOverflow().url())
                .requestFactory(externalApiRequestFactory)
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(StackOverflowClient.class);
    }

    @Bean
    public BotClient botClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(
                        scrapperConfig.botUrl() == null ? BASE_BOT_URL : scrapperConfig.botUrl())
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(BotClient.class);
    }

    @Bean
    public ReactorClientHttpRequestFactory externalApiRequestFactory() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("external-api-client")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofMinutes(2))
                .evictInBackground(Duration.ofSeconds(30))
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .compress(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(Duration.ofSeconds(READ_WRITE_TIMEOUT_SECONDS))
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(READ_WRITE_TIMEOUT_SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(READ_WRITE_TIMEOUT_SECONDS)));
        return new ReactorClientHttpRequestFactory(httpClient);
    }
}
