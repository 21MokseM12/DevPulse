package backend.academy.scrapper.integration_test;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {ScrapperApplication.class})
@ActiveProfiles("test")
public class TestApplication extends TestContainersConfiguration {

    @Test
    public void contextLoads() {

    }
}
