package backend.academy.bot.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@EnableConfigurationProperties
public class ClientConfigTest {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private LinkClient linkClient;

    @Test
    public void testChatClientBean() {
        assertThat(chatClient).isNotNull();
    }

    @Test
    public void testLinkClientBean() {
        assertThat(linkClient).isNotNull();
    }
}
