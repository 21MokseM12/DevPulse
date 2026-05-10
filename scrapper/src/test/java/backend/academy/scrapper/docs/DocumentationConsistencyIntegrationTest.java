package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DocumentationConsistencyIntegrationTest {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([A-Z0-9_]+)");

    private static final Path ROOT = Path.of("..");
    private static final Path ENV_TEMPLATE_PATH = ROOT.resolve(".env.example");
    private static final Path README_PATH = ROOT.resolve("README.md");
    private static final Path BOT_APPLICATION_PATH = ROOT.resolve("bot/src/main/resources/application.yaml");
    private static final Path SCRAPPER_APPLICATION_PATH = ROOT.resolve("scrapper/src/main/resources/application.yaml");

    @Test
    void envTemplate_shouldCoverAllPlaceholdersFromBackendApplications() throws IOException {
        Set<String> envKeys = EnvExampleParser.extractKeys(read(ENV_TEMPLATE_PATH));
        Set<String> placeholders = new LinkedHashSet<>();
        placeholders.addAll(extractPlaceholders(read(BOT_APPLICATION_PATH)));
        placeholders.addAll(extractPlaceholders(read(SCRAPPER_APPLICATION_PATH)));

        for (String placeholder : placeholders) {
            assertTrue(envKeys.contains(placeholder), () -> ".env.example misses key: " + placeholder);
        }
    }

    @Test
    void readme_shouldDescribeQuickStartDeliveryModesAndHealthChecks() throws IOException {
        String readme = read(README_PATH);

        assertTrue(ReadmeChecklistValidator.hasRequiredRuntimeSections(readme));
        assertTrue(ReadmeChecklistValidator.hasDeliveryModeDetails(readme));
        assertTrue(ReadmeChecklistValidator.hasResilienceGuide(readme));
        assertTrue(readme.contains("docker compose up --build"));
        assertTrue(readme.contains("./mvnw clean verify"));
        assertTrue(readme.contains("http://localhost:8080/actuator/health"));
        assertTrue(readme.contains("http://localhost:8081/actuator/health"));
        assertTrue(readme.contains("docs/runbooks/README.md"));
        assertTrue(readme.contains("docs/runbooks/post-deploy-smoke.md"));
    }

    private static Set<String> extractPlaceholders(String content) {
        Set<String> placeholders = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
