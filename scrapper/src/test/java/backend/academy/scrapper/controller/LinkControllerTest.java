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

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
    private static final String INTERNAL_SECRET = "devpulse-internal-secret";

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

        mockMvc.perform(get("/links").header("Client-Login", "alice").header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET))
                .andExpect(status().isOk());

        verify(chatOperationProcessor).findClientIdByLogin("alice");
        verify(linkProcessor).findAll(42L);
    }

    @Test
    void get_links_returns404WhenClientUnknown() throws Exception {
        when(chatOperationProcessor.findClientIdByLogin("nobody")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/links").header("Client-Login", "nobody").header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_links_subscribesWithoutPasswordHeader() throws Exception {
        URI link = URI.create("https://github.com/spring-projects/spring-framework");
        when(chatOperationProcessor.findClientIdByLogin("alice")).thenReturn(java.util.Optional.of(7L));
        when(linkProcessor.subscribeLink(eq(7L), any(AddLinkRequest.class)))
                .thenReturn(new LinkResponse(1L, link, Set.of(), Set.of()));

        mockMvc.perform(post("/links")
                        .header("Client-Login", "alice")
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
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
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"" + link + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void post_links_returns400WhenDuplicateTagsProvided() throws Exception {
        mockMvc.perform(post("/links")
                        .header("Client-Login", "alice")
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://example.com/x\",\"tags\":[\"a\",\"a\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_links_returns400WhenLinkIsRelative() throws Exception {
        mockMvc.perform(post("/links")
                        .header("Client-Login", "alice")
                        .header(INTERNAL_SECRET_HEADER, INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"/relative/path\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_links_returns401WithoutInternalSecret() throws Exception {
        mockMvc.perform(get("/links").header("Client-Login", "alice")).andExpect(status().isUnauthorized());
    }
}
