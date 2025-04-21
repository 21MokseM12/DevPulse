package backend.academy.scrapper.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.service.validators.LinkValidatorManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;
import scrapper.bot.connectivity.model.response.ListLinkResponse;

@WebMvcTest(controllers = LinkController.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkService linkService;

    @MockitoBean
    private LinkValidatorManager linkValidatorManager;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testFindAllLinksSuccessfully() throws Exception {
        Long id = 123L;

        List<LinkResponse> links =
                List.of(new LinkResponse(1L, URI.create("simple-uri"), Set.of("tag"), Set.of("filter")));
        ListLinkResponse expected = new ListLinkResponse(links, 1);

        when(linkService.findAllByChatId(id)).thenReturn(links);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(expected.size()))
                .andExpect(content().json(mapper.writeValueAsString(expected)));

        verify(linkService, times(1)).findAllByChatId(id);
    }

    @Test
    public void testFindAllLinksBadRequestException() throws Exception {
        Long id = 123L;

        when(linkService.findAllByChatId(id)).thenReturn(List.of());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", id)).andExpect(status().isOk());

        verify(linkService, times(1)).findAllByChatId(id);
    }

    @Test
    public void testSubscribeLinkSuccessfully() throws Exception {
        Long id = 123L;
        LinkResponse response = new LinkResponse(1L, URI.create("simple-uri"), Set.of("tag"), Set.of("filter"));
        AddLinkRequest linkRequest = new AddLinkRequest(URI.create("uri"), Set.of("tag"), Set.of("filter"));

        when(linkValidatorManager.isValidLink(linkRequest.link().toString())).thenReturn(true);
        when(linkService.subscribe(id, linkRequest)).thenReturn(Optional.of(response));

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(response)));

        verify(linkService, times(1)).subscribe(id, linkRequest);
        verify(linkValidatorManager, times(1)).isValidLink(linkRequest.link().toString());
    }

    @Test
    public void testSubscribeLinkBadRequestExceptionViaInvalidLink() throws Exception {
        Long id = 123L;
        AddLinkRequest linkRequest = new AddLinkRequest(URI.create("uri"), Set.of("tag"), Set.of("filter"));

        when(linkValidatorManager.isValidLink(linkRequest.link().toString())).thenReturn(false);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkRequest)))
                .andExpect(status().isBadRequest());

        verify(linkService, times(0)).subscribe(id, linkRequest);
        verify(linkValidatorManager, times(1)).isValidLink(linkRequest.link().toString());
    }

    @Test
    public void testSubscribeLinkBadRequestExceptionViaOptionalIsEmpty() throws Exception {
        Long id = 123L;
        AddLinkRequest linkRequest = new AddLinkRequest(URI.create("uri"), Set.of("tag"), Set.of("filter"));

        when(linkValidatorManager.isValidLink(linkRequest.link().toString())).thenReturn(true);
        when(linkService.subscribe(id, linkRequest)).thenReturn(Optional.empty());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkRequest)))
                .andExpect(status().isBadRequest());

        verify(linkService, times(1)).subscribe(id, linkRequest);
        verify(linkValidatorManager, times(1)).isValidLink(linkRequest.link().toString());
    }

    @Test
    public void testUnsubscribeLinkSuccessfully() throws Exception {
        Long id = 123L;
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("uri"));
        LinkResponse response = new LinkResponse(1L, URI.create("uri"), Set.of("tag"), Set.of("filter"));

        when(linkValidatorManager.isValidLink(removeLinkRequest.link().toString()))
                .thenReturn(true);
        when(linkService.unsubscribe(id, removeLinkRequest)).thenReturn(Optional.of(response));

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(removeLinkRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(response)));

        verify(linkService, times(1)).unsubscribe(id, removeLinkRequest);
        verify(linkValidatorManager, times(1))
                .isValidLink(removeLinkRequest.link().toString());
    }

    @Test
    public void testUnsubscribeLinkBadRequestException() throws Exception {
        Long id = 123L;
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("uri"));

        when(linkValidatorManager.isValidLink(removeLinkRequest.link().toString()))
                .thenReturn(false);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(removeLinkRequest)))
                .andExpect(status().isBadRequest());

        verify(linkService, times(0)).unsubscribe(id, removeLinkRequest);
        verify(linkValidatorManager, times(1))
                .isValidLink(removeLinkRequest.link().toString());
    }

    @Test
    public void testUnsubscribeLinkResourceNotFoundException() throws Exception {
        Long id = 123L;
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("uri"));

        when(linkValidatorManager.isValidLink(removeLinkRequest.link().toString()))
                .thenReturn(true);
        when(linkService.unsubscribe(id, removeLinkRequest)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(removeLinkRequest)))
                .andExpect(status().isNotFound());

        verify(linkService, times(1)).unsubscribe(id, removeLinkRequest);
        verify(linkValidatorManager, times(1))
                .isValidLink(removeLinkRequest.link().toString());
    }
}
