package backend.academy.bot.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scrapper.bot.connectivity.model.LinkUpdate;

class LinkUpdateJsonTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void linkUpdate_roundTrip_usesClientsIdsFieldName() throws Exception {
        LinkUpdate original =
                new LinkUpdate(
                        1L,
                        URI.create("https://example.com/a"),
                        "t",
                        "o",
                        "d",
                        OffsetDateTime.parse("2026-04-05T10:00:00Z"),
                        List.of(10L, 20L));

        String json = mapper.writeValueAsString(original);
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("clientsIds"));

        LinkUpdate parsed = mapper.readValue(json, LinkUpdate.class);
        assertEquals(original, parsed);
    }
}
