package backend.academy.scrapper.config;

import backend.academy.scrapper.config.properties.DatabaseProperty;
import backend.academy.scrapper.config.properties.RedisCacheProperty;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ScrapperConfig.class,
    DatabaseProperty.class,
    RedisCacheProperty.class,
})
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
