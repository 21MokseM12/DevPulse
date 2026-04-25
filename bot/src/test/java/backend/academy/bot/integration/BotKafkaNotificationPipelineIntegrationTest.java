package backend.academy.bot.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
        "app.kafka.consumers.link-updates.topic=bot-link-updates-test",
        "app.kafka.consumers.link-updates.group-id=bot-link-updates-test-group",
        "app.kafka.retry-policy.max-attempts=1"
})
class BotKafkaNotificationPipelineIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("devpulse_bot_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("app.scrapper-url", () -> "http://localhost:9999");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM notification_recipients");
        jdbcTemplate.update("DELETE FROM notifications");
        jdbcTemplate.update("DELETE FROM clients");
    }

    @Test
    void kafkaAndHttp_useSamePipelineAndStoreSingleNotification() throws Exception {
        registerClient("1", "1");
        String payload = objectMapper.writeValueAsString(Map.of(
                "id", 555,
                "url", "https://github.com/org/repo/pull/555",
                "title", "PR updated",
                "updateOwner", "octocat",
                "description", "description",
                "creationDate", OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                "clientsIds", List.of(1)));

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        kafkaTemplate.send("bot-link-updates-test", payload);

        Awaitility.await().untilAsserted(() -> {
            Integer notifications = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE link_id = 555", Integer.class);
            Integer recipients = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notification_recipients WHERE client_login = '1'", Integer.class);
            assertThat(notifications).isEqualTo(1);
            assertThat(recipients).isEqualTo(1);
        });
    }

    @Test
    void kafkaInvalidPayload_isNotStored() {
        kafkaTemplate.send("bot-link-updates-test", "{ invalid json }");

        Awaitility.await().untilAsserted(() -> {
            Integer notifications = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notifications", Integer.class);
            assertThat(notifications).isZero();
        });
    }

    private void registerClient(String login, String password) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("login", login, "password", password));
        mockMvc.perform(post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());
    }
}
