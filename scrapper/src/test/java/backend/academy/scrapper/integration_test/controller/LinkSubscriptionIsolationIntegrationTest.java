package backend.academy.scrapper.integration_test.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ScrapperApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LinkSubscriptionIsolationIntegrationTest extends TestContainersConfiguration {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
    private static final String INTERNAL_SECRET = "devpulse-internal-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void subscribe_sameLinkForDifferentUsers_doesNotLeakTagsAndFiltersInResponse() throws Exception {
        String firstLogin = "first-" + UUID.randomUUID();
        String secondLogin = "second-" + UUID.randomUUID();
        String link = "https://github.com/devpulse/shared-link";

        createClient(firstLogin);
        createClient(secondLogin);

        mockMvc.perform(post("/links")
                        .header("Client-Login", firstLogin)
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"" + link + "\",\"tags\":[\"first-tag\"],\"filters\":[\"author:first\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasItem("first-tag")))
                .andExpect(jsonPath("$.filters", hasItem("author:first")));

        mockMvc.perform(post("/links")
                        .header("Client-Login", secondLogin)
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"" + link + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", empty()))
                .andExpect(jsonPath("$.filters", empty()));
    }

    private void createClient(String login) {
        jdbcTemplate.update("insert into clients (login, password_hash) values (?, ?)", login, "hash");
    }
}
