package backend.academy.scrapper.config;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    private static final String BASE_GITHUB_URL = "https://api.github.com";

    private static final String BASE_BOT_URL = "https://api.github.com";

    private static final String BASE_STACKOVERFLOW_URL = "https://api.stackexchange.com/2.3";

    @Autowired
    private ScrapperConfig scrapperConfig;

    @Bean
    public GithubClient githubClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(
                        scrapperConfig.github().url() == null
                                ? BASE_GITHUB_URL
                                : scrapperConfig.github().url())
                .defaultHeader(
                        "Authorization", "token " + scrapperConfig.github().token())
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(GithubClient.class);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(
                        scrapperConfig.stackOverflow().url() == null
                                ? BASE_STACKOVERFLOW_URL
                                : scrapperConfig.stackOverflow().url())
                .defaultHeader(
                        "Authorization", "token " + scrapperConfig.github().token())
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
}
