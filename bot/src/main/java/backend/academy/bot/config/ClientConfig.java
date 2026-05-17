package backend.academy.bot.config;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    private static final String BASE_SCRAPPER_URL = "http://localhost:8081";
    private static final String INTERNAL_AUTH_HEADER = "X-Internal-Secret";

    private final ApplicationConfig config;

    @Autowired
    public ClientConfig(ApplicationConfig config) {
        this.config = config;
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(resolveScrapperBaseUrl())
                .defaultHeader(INTERNAL_AUTH_HEADER, config.sharedSecret())
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
    }

    private String resolveScrapperBaseUrl() {
        String configuredUrl = config.scrapperUrl();
        if (configuredUrl == null || configuredUrl.isBlank()) {
            return BASE_SCRAPPER_URL;
        }

        String normalizedUrl = configuredUrl.trim();
        if (normalizedUrl.contains("{") || normalizedUrl.contains("}")) {
            throw new IllegalStateException("Invalid app.scrapper-url value: unresolved placeholder detected. "
                    + "Set BOT_SCRAPPER_URL (or SCRAPPER_URL) to explicit URL, e.g. http://scrapper:8081");
        }

        URI.create(normalizedUrl);
        return normalizedUrl;
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
