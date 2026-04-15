package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public class ApplicationConfig {

    private String telegramToken;

    @NotEmpty
    private String scrapperUrl;

    private boolean telegramEnabled;

    public void setTelegramToken(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    public void setScrapperUrl(String scrapperUrl) {
        this.scrapperUrl = scrapperUrl;
    }

    public void setTelegramEnabled(boolean telegramEnabled) {
        this.telegramEnabled = telegramEnabled;
    }
}
