package backend.academy.scrapper.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClient;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ClientConfigTest {

    @Autowired
    private ClientConfig clientConfig;

    @Autowired
    private RestClient.Builder builder;

    @MockitoBean
    private BotClient botClient;

    @MockitoBean
    private GithubClient githubClient;

    @MockitoBean
    private StackOverflowClient stackOverflowClient;

    @Test
    public void testBotClientBean() {
        BotClient client = clientConfig.botClient(builder);
        assertThat(client).isNotNull();
    }

    @Test
    public void testGithubClientBean() {
        GithubClient client = clientConfig.githubClient(builder);
        assertThat(client).isNotNull();
    }

    @Test
    public void testStackOverflowClientBean() {
        StackOverflowClient client = clientConfig.stackOverflowClient(builder);
        assertThat(client).isNotNull();
    }
}
