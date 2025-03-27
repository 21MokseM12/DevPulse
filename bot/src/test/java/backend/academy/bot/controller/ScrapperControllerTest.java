package backend.academy.bot.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.service.notifications.BotNotificationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import scrapper.bot.connectivity.model.LinkUpdate;

@WebMvcTest(controllers = ScrapperController.class)
public class ScrapperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BotNotificationManager notificationManager;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testGetUpdatedLinkSuccess() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("uri"),
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of(1L, 2L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isOk());

        verify(notificationManager).notify(linkUpdate);
    }

    @Test
    public void testGetUpdatedLinkBadRequestExceptionViaEmptyIdsList() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("uri"),
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of());

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest());

        verify(notificationManager, times(0)).notify(linkUpdate);
    }

    @Test
    public void testGetUpdatedLinkBadRequestExceptionViaLinkISNull() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                null,
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of(1L, 2L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest());

        verify(notificationManager, times(0)).notify(linkUpdate);
    }
}
