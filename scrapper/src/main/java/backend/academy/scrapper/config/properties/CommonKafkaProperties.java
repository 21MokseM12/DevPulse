package backend.academy.scrapper.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.kafka.listener.ContainerProperties;

@Valid
@ConfigurationProperties(prefix = "kafka.properties")
public record CommonKafkaProperties(
    @NotBlank String bootstrapServers,
    @NotNull ConsumerProperties consumer,
    @NotNull ProducerProperties producer
) {
    public record ConsumerProperties(
        boolean enableAutoCommit,
        @NotNull ContainerProperties.AckMode ackMode,
        @NotBlank String autoOffsetReset,
        @NotBlank String trustedPackages
    ) { }

    public record ProducerProperties(
        @NotBlank String clientId,
        @NotBlank String acksConfig,
        boolean enableIdempotenceConfig
    ) { }
}
