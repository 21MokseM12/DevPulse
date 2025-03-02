package backend.academy.scrapper.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ScrapperConfigTest {

    @Autowired
    private ScrapperConfig applicationConfig;

    @Test
    public void testLoadYamlConfiguration() {
        assertThat(applicationConfig.botUrl()).isEqualTo("https://example.eu");

        assertThat(applicationConfig.scheduler().interval()).isEqualTo(Duration.of(15, ChronoUnit.SECONDS));
        assertThat(applicationConfig.scheduler().forceCheckDelay()).isEqualTo(Duration.of(30, ChronoUnit.SECONDS));

        assertThat(applicationConfig.github().url()).isEqualTo("https://example.ru");
        assertThat(applicationConfig.github().token()).isEqualTo("github-token");

        assertThat(applicationConfig.stackOverflow().url()).isEqualTo("https://example.com");
        assertThat(applicationConfig.stackOverflow().accessToken()).isEqualTo("stackoverflow-access-token");
        assertThat(applicationConfig.stackOverflow().key()).isEqualTo("stackoverflow-key");
    }
}
