package backend.academy.bot.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ApplicationConfig.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@EnableConfigurationProperties
public class ApplicationConfigTest {

    @Autowired
    private ApplicationConfig applicationConfig;

    @Test
    public void testLoadYamlConfiguration() {
        assertThat(applicationConfig.scrapperUrl()).isEqualTo("https://api.github.com");
        assertThat(applicationConfig.telegramToken()).isEqualTo("simple-token-example");
    }
}
