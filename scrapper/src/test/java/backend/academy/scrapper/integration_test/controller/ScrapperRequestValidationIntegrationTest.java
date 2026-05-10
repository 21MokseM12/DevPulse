package backend.academy.scrapper.integration_test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ScrapperApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScrapperRequestValidationIntegrationTest extends TestContainersConfiguration {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
    private static final String INTERNAL_SECRET = "devpulse-internal-secret";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void linksSubscribe_returns400ForDuplicateTags() throws Exception {
        mockMvc.perform(post("/links")
                        .header("Client-Login", "unknown-user")
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://example.com/resource\",\"tags\":[\"a\",\"a\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void linksSubscribe_returns400ForRelativeUri() throws Exception {
        mockMvc.perform(post("/links")
                        .header("Client-Login", "unknown-user")
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"/relative/path\"}"))
                .andExpect(status().isBadRequest());
    }
}
