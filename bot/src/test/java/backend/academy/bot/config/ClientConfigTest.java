package backend.academy.bot.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ClientConfigTest {

    @Autowired
    private ClientConfig clientConfig;

    @MockitoBean
    private HttpServiceProxyFactory httpServiceProxyFactory;

    @Test
    public void testChatClientBean() {
        ChatClient client = clientConfig.chatClient(httpServiceProxyFactory);
        assertThat(client).isNotNull();
    }

    @Test
    public void testLinkClientBean() {
        LinkClient client = clientConfig.linkClient(httpServiceProxyFactory);
        assertThat(client).isNotNull();
    }
}
