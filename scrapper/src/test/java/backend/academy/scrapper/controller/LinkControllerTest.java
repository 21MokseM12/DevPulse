package backend.academy.scrapper.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.service.ChatOperationProcessor;
import backend.academy.scrapper.service.LinkProcessor;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@ActiveProfiles("test")
@WebMvcTest(controllers = LinkController.class)
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatOperationProcessor chatOperationProcessor;

    @MockitoBean
    private LinkProcessor linkProcessor;

    @Test
    void get_links_usesOnlyClientLogin_proxiesToProcessor() throws Exception {
        when(chatOperationProcessor.findClientIdByLogin("alice")).thenReturn(java.util.Optional.of(42L));
        when(linkProcessor.findAll(42L)).thenReturn(List.of());

        mockMvc.perform(get("/links").header("Client-Login", "alice")).andExpect(status().isOk());

        verify(chatOperationProcessor).findClientIdByLogin("alice");
        verify(linkProcessor).findAll(42L);
    }

    @Test
    void get_links_returns404WhenClientUnknown() throws Exception {
        when(chatOperationProcessor.findClientIdByLogin("nobody")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/links").header("Client-Login", "nobody")).andExpect(status().isNotFound());
    }

    @Test
    void post_links_subscribesWithoutPasswordHeader() throws Exception {
        URI link = URI.create("https://github.com/spring-projects/spring-framework");
        when(chatOperationProcessor.findClientIdByLogin("alice")).thenReturn(java.util.Optional.of(7L));
        when(linkProcessor.subscribeLink(eq(7L), any(AddLinkRequest.class)))
                .thenReturn(new LinkResponse(1L, link, Set.of(), Set.of()));

        mockMvc.perform(post("/links")
                        .header("Client-Login", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"" + link + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_links_unsubscribesWithoutPasswordHeader() throws Exception {
        URI link = URI.create("https://example.com/x");
        when(chatOperationProcessor.findClientIdByLogin("alice")).thenReturn(java.util.Optional.of(7L));
        when(linkProcessor.unsubscribeLink(eq(7L), any(RemoveLinkRequest.class)))
                .thenReturn(new LinkResponse(1L, link, Set.of(), Set.of()));

        mockMvc.perform(delete("/links")
                        .header("Client-Login", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"" + link + "\"}"))
                .andExpect(status().isOk());
    }
}
