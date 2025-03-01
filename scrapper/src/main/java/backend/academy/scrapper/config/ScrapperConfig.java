package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    GitHubCredentials github,
    StackOverflowCredentials stackOverflow,
    @NotEmpty String botUrl
) {

    public record GitHubCredentials(@NotEmpty String token, @NotEmpty String url) {}

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
}
