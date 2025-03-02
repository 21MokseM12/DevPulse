package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        GitHubCredentials github,
        StackOverflowCredentials stackOverflow,
        @NotEmpty String botUrl,
        @NotNull @Bean SchedulerCredentials scheduler) {

    public record SchedulerCredentials(@NotEmpty Duration interval, @NotEmpty Duration forceCheckDelay) {}

    public record GitHubCredentials(@NotEmpty String token, @NotEmpty String url) {}

    public record StackOverflowCredentials(@NotEmpty String url, @NotEmpty String key, @NotEmpty String accessToken) {}
}
