package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig {

    @NotEmpty
    private String scrapperUrl;

    @NotEmpty
    private String sharedSecret;

    @NotEmpty
    private String internalHeader;

    public void setScrapperUrl(String scrapperUrl) {
        this.scrapperUrl = scrapperUrl;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public void setInternalHeader(String internalHeader) {
        this.internalHeader = internalHeader;
    }
}
