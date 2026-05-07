package backend.academy.scrapper.config.properties;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cache.redis")
public record RedisCacheProperty(boolean enabled, @NotNull Duration etagTtl, @NotNull Duration pollHintTtl) {}
