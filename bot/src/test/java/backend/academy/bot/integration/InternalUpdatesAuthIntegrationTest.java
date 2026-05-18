package backend.academy.bot.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class InternalUpdatesAuthIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("devpulse_bot_updates_auth_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.shared-secret", () -> "integration-secret");
        registry.add("app.internal-header", () -> "X-Internal-Secret");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void updatesWithoutHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(validLinkUpdateJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.exceptionName").value("MissingInternalAuthHeaderException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Missing internal auth header"))
                .andExpect(jsonPath("$.stacktrace").isArray());
    }

    @Test
    void updatesWithWrongHeader_returnsForbidden() throws Exception {
        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLinkUpdateJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.description").value("Forbidden"))
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.exceptionName").value("InvalidInternalAuthSecretException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Invalid internal auth secret"))
                .andExpect(jsonPath("$.stacktrace").isArray())
                .andExpect(content().string(Matchers.not(Matchers.containsString("wrong"))));
    }

    @Test
    void updatesWithValidHeader_returnsOk() throws Exception {
        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "integration-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLinkUpdateJson()))
                .andExpect(status().isOk());
    }

    private String validLinkUpdateJson() {
        return "{"
                + "\"id\":1,"
                + "\"url\":\"https://github.com/org/repo\","
                + "\"title\":\"title\","
                + "\"updateOwner\":\"owner\","
                + "\"description\":\"description\","
                + "\"creationDate\":\"" + OffsetDateTime.parse("2026-01-01T00:00:00Z") + "\","
                + "\"clientLogins\":[\"user-101\"]"
                + "}";
    }
}
