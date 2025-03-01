package backend.academy.scrapper;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.SpringApplication;

@Disabled
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(ScrapperApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
