package backend.academy.bot.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.service.notifications.BotNotificationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
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

    @Test
    public void testGetUpdatedLinkSuccess() throws Exception {
        LinkUpdate update = new LinkUpdate(1L, URI.create("uri"), "description", List.of(1L, 2L));
        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        verify(notificationManager).notify(update);
    }

    @Test
    public void testGetUpdatedLinkBadRequestExceptionViaEmptyIdsList() throws Exception {
        LinkUpdate update = new LinkUpdate(1L, URI.create("uri"), "description", List.of());
        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest());

        verify(notificationManager, times(0)).notify(update);
    }

    @Test
    public void testGetUpdatedLinkBadRequestExceptionViaLinkISNull() throws Exception {
        LinkUpdate update = new LinkUpdate(1L, null, "description", List.of(1L));
        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest());

        verify(notificationManager, times(0)).notify(update);
    }
}
