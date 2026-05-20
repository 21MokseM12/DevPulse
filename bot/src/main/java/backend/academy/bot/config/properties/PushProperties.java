package backend.academy.bot.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.push")
public record PushProperties(
        @NotNull @Valid FcmProperties fcm,
        @NotNull @Valid RetryProperties retry,
        @Min(1) int tokenApiRateLimitPerMinute) {

    public record FcmProperties(String projectId, String credentialsPath, String endpointTemplate) {}

    public record RetryProperties(
            @Min(1) int maxAttempts,
            @Min(1) long initialDelayMs,
            @DecimalMin("1.0") double multiplier,
            @Min(1) long maxDelayMs) {}
}
