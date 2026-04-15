package backend.academy.bot.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

class ClientConfigTest {

    @Test
    void httpServiceProxyFactory_usesDefaultUrlWhenConfigIsNull() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        ClientConfig clientConfig = new ClientConfig(applicationConfig);

        HttpServiceProxyFactory factory = clientConfig.httpServiceProxyFactory(RestClient.builder());
        ChatClient chatClient = clientConfig.chatClient(factory);
        LinkClient linkClient = clientConfig.linkClient(factory);

        assertNotNull(factory);
        assertNotNull(chatClient);
        assertNotNull(linkClient);
    }

    @Test
    void httpServiceProxyFactory_usesConfiguredUrlWhenProvided() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setScrapperUrl("http://example.com");
        ClientConfig clientConfig = new ClientConfig(applicationConfig);

        HttpServiceProxyFactory factory = clientConfig.httpServiceProxyFactory(RestClient.builder());

        assertNotNull(factory);
    }
}
