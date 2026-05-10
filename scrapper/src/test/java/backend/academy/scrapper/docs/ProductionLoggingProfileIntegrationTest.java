package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProductionLoggingProfileIntegrationTest {

    private static final Path ROOT = Path.of("..");
    private static final Path BOT_APPLICATION_PROD = ROOT.resolve("bot/src/main/resources/application-prod.yaml");
    private static final Path SCRAPPER_APPLICATION_PROD =
            ROOT.resolve("scrapper/src/main/resources/application-prod.yaml");
    private static final Path BOT_LOGBACK_PROD = ROOT.resolve("bot/src/main/resources/logback-prod.xml");
    private static final Path SCRAPPER_LOGBACK_PROD = ROOT.resolve("scrapper/src/main/resources/logback-prod.xml");

    @Test
    void productionProfiles_shouldUseDedicatedLogbackConfig() throws IOException {
        assertTrue(read(BOT_APPLICATION_PROD).contains("classpath:logback-prod.xml"));
        assertTrue(read(SCRAPPER_APPLICATION_PROD).contains("classpath:logback-prod.xml"));
    }

    @Test
    void productionLogback_shouldUseJsonEncoderAndMaskSecrets() throws IOException {
        String botLogback = read(BOT_LOGBACK_PROD);
        String scrapperLogback = read(SCRAPPER_LOGBACK_PROD);

        assertTrue(botLogback.contains("LoggingEventCompositeJsonEncoder"));
        assertTrue(scrapperLogback.contains("LoggingEventCompositeJsonEncoder"));
        assertTrue(botLogback.contains("\"traceId\":\"%X{traceId:-}\""));
        assertTrue(scrapperLogback.contains("\"traceId\":\"%X{traceId:-}\""));
        assertTrue(botLogback.contains("%maskedMessage"));
        assertTrue(scrapperLogback.contains("%maskedMessage"));
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
