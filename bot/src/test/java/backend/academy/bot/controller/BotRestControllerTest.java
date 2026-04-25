package backend.academy.bot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.model.entity.LinkDTO;
import backend.academy.bot.service.ClientOperationService;
import backend.academy.bot.service.ScrapperConnectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import scrapper.bot.connectivity.model.response.LinkResponse;

@WebMvcTest(controllers = BotRestController.class)
class BotRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScrapperConnectionService scrapperConnectionService;

    @MockitoBean
    private ClientOperationService clientOperationService;

    @Test
    void registerClient_returnsWelcomeMessage() throws Exception {
        var payload = """
                {
                  "login": "user",
                  "password": "pass"
                }
                """;

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Messages.WELCOME_MESSAGE.toString()));

        verify(clientOperationService).registerClient("user", "pass");
    }

    @Test
    void unregisterClient_returnsDeleteMessage() throws Exception {
        var payload = """
                {
                  "login": "user",
                  "password": "pass"
                }
                """;

        mockMvc.perform(delete("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Messages.DELETE_SUBSCRIBE_MESSAGE.toString()));

        verify(clientOperationService).unregisterClient("user", "pass");
    }

    @Test
    void unregisterClient_returns404WhenChatNotFound() throws Exception {
        var payload = """
                {
                  "login": "user",
                  "password": "pass"
                }
                """;
        doThrow(new ChatNotFoundException(Messages.ERROR.toString()))
                .when(clientOperationService)
                .unregisterClient("user", "pass");

        mockMvc.perform(delete("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    void getLinks_returnsLinksList() throws Exception {
        var links = List.of(new LinkResponse(1L, URI.create("https://github.com/u/r"), Set.of("java"), Set.of("f")));
        when(scrapperConnectionService.getAllLinks("user")).thenReturn(links);

        mockMvc.perform(get("/api/v1/links").header("Client-Login", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].url").value("https://github.com/u/r"));
    }

    @Test
    void trackLink_mapsRequestToLinkDtoAndReturnsResponse() throws Exception {
        var addRequest = """
                {
                  "link": "https://github.com/u/r",
                  "tags": ["java", "spring"],
                  "filters": ["f1"]
                }
                """;
        var response = new LinkResponse(5L, URI.create("https://github.com/u/r"), Set.of("java", "spring"), Set.of("f1"));
        when(scrapperConnectionService.subscribeLink(eq("user"), any(LinkDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/links")
                        .header("Client-Login", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.url").value("https://github.com/u/r"));

        ArgumentCaptor<LinkDTO> captor = ArgumentCaptor.forClass(LinkDTO.class);
        verify(scrapperConnectionService).subscribeLink(eq("user"), captor.capture());
        LinkDTO captured = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("https://github.com/u/r", captured.uri());
        org.junit.jupiter.api.Assertions.assertEquals(Set.of("java", "spring"), captured.tags());
        org.junit.jupiter.api.Assertions.assertEquals(Set.of("f1"), captured.filters());
    }

    @Test
    void untrackLink_returnsDeleteMessageWhenUnsubscribed() throws Exception {
        URI url = URI.create("https://github.com/u/r");
        var links = List.of(new LinkResponse(10L, url, Set.of(), Set.of()));
        when(scrapperConnectionService.getAllLinks("user")).thenReturn(links);
        when(scrapperConnectionService.unsubscribeLink("user", links, 10L)).thenReturn(true);

        var request = objectMapper.writeValueAsString(new scrapper.bot.connectivity.model.request.RemoveLinkRequest(url));
        mockMvc.perform(delete("/api/v1/links")
                        .header("Client-Login", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Messages.DELETE_SUBSCRIBE_MESSAGE.toString()));
    }

    @Test
    void untrackLink_returnsBadRequestWhenNoMatchingLink() throws Exception {
        when(scrapperConnectionService.getAllLinks("user"))
                .thenReturn(List.of(new LinkResponse(10L, URI.create("https://github.com/u/r"), Set.of(), Set.of())));

        var request = """
                {
                  "link": "https://github.com/u/another"
                }
                """;
        mockMvc.perform(delete("/api/v1/links")
                        .header("Client-Login", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
