package backend.academy.bot.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

class SecretMaskingConverterTest {

    private final SecretMaskingConverter converter = new SecretMaskingConverter();

    @Test
    void convert_masksKnownSecretKeys() {
        LoggingEvent event = new LoggingEvent();
        event.setMessage("password=qwerty token:abc123 secret my-secret apiKey=prod-key");

        String masked = converter.convert(event);

        assertThat(masked).isEqualTo("password=*** token:*** secret *** apiKey=***");
    }

    @Test
    void convert_keepsNonSensitiveTextUntouched() {
        LoggingEvent event = new LoggingEvent();
        event.setMessage("delivery completed for chatId=101");

        String masked = converter.convert(event);

        assertThat(masked).isEqualTo("delivery completed for chatId=101");
    }
}
