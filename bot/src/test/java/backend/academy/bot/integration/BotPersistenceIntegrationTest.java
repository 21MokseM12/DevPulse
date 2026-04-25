package backend.academy.bot.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class BotPersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("devpulse_bot_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.scrapper-url", () -> "http://localhost:9999");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM notification_recipients");
        jdbcTemplate.update("DELETE FROM notifications");
        jdbcTemplate.update("DELETE FROM clients");
    }

    @Test
    void registerAndDeleteClient_persistsClientInDatabase() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("login", "user-1", "password", "pass-1"));

        mockMvc.perform(post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        Integer existingAfterRegister =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clients WHERE login = 'user-1'", Integer.class);
        assertThat(existingAfterRegister).isEqualTo(1);

        mockMvc.perform(delete("/api/v1/clients").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        Integer existingAfterDelete =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clients WHERE login = 'user-1'", Integer.class);
        assertThat(existingAfterDelete).isZero();
    }

    @Test
    void postUpdates_savesNotification() throws Exception {
        registerClient("1", "1");

        String updatePayload = objectMapper.writeValueAsString(Map.of(
                "id", 101,
                "url", "https://github.com/org/repo/issues/101",
                "title", "Issue updated",
                "updateOwner", "octocat",
                "description", "Details",
                "creationDate", OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                "clientsIds", List.of(1)));

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isOk());

        Integer notifications =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notifications WHERE link_id = 101", Integer.class);
        Integer recipients = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notification_recipients WHERE client_login = '1'", Integer.class);
        assertThat(notifications).isEqualTo(1);
        assertThat(recipients).isEqualTo(1);
    }

    @Test
    void deleteClient_cascadesRecipientRows() throws Exception {
        registerClient("1", "1");

        String updatePayload = objectMapper.writeValueAsString(Map.of(
                "id", 202,
                "url", "https://github.com/org/repo/pull/202",
                "title", "PR updated",
                "updateOwner", "octocat",
                "description", "Details",
                "creationDate", OffsetDateTime.parse("2026-04-26T00:00:00Z"),
                "clientsIds", List.of(1)));

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isOk());

        Integer recipientsBeforeDelete = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notification_recipients WHERE client_login = '1'", Integer.class);
        assertThat(recipientsBeforeDelete).isEqualTo(1);

        String clientPayload = objectMapper.writeValueAsString(Map.of("login", "1", "password", "1"));
        mockMvc.perform(delete("/api/v1/clients").contentType(MediaType.APPLICATION_JSON).content(clientPayload))
                .andExpect(status().isOk());

        Integer recipientsAfterDelete = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notification_recipients WHERE client_login = '1'", Integer.class);
        assertThat(recipientsAfterDelete).isZero();
    }

    private void registerClient(String login, String password) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("login", login, "password", password));
        mockMvc.perform(post("/api/v1/clients").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());
    }
}
