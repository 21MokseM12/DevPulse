package backend.academy.scrapper.config;

import backend.academy.scrapper.client.GithubClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Autowired
    private ScrapperConfig scrapperConfig;

    @Bean
    public GithubClient getGithubClient(RestClient.Builder builder) {
        RestClient restClient = builder
            .baseUrl(scrapperConfig.github().url())
            .defaultHeader("Authorization", "token " + scrapperConfig.github().token())
            .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();
        return factory.createClient(GithubClient.class);
    }
}
