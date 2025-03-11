package backend.academy.bot.config;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    private static final String BASE_SCRAPPER_URL = "http://localhost:8081";

    private final ApplicationConfig config;

    @Autowired
    public ClientConfig(ApplicationConfig config) {
        this.config = config;
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(config.scrapperUrl() == null ? BASE_SCRAPPER_URL : config.scrapperUrl())
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
    }

    @Bean
    public ChatClient chatClient(HttpServiceProxyFactory factory) {
        return factory.createClient(ChatClient.class);
    }

    @Bean
    public LinkClient linkClient(HttpServiceProxyFactory factory) {
        return factory.createClient(LinkClient.class);
    }
}
