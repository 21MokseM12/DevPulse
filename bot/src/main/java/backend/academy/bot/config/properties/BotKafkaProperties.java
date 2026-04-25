package backend.academy.bot.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
public record BotKafkaProperties(
        @NotNull @Valid ConsumersProperties consumers,
        @NotNull @Valid RetryPolicyProperties retryPolicy) {

    public record ConsumersProperties(@NotNull @Valid LinkUpdatesConsumerProperties linkUpdates) {}

    public record LinkUpdatesConsumerProperties(@NotBlank String topic, @NotBlank String groupId) {}

    public record RetryPolicyProperties(
            @Min(1) long interval,
            double multiplier,
            @Min(1) long maxDelay,
            @Min(1) int maxAttempts,
            boolean autoCreateTopics) {}
}
