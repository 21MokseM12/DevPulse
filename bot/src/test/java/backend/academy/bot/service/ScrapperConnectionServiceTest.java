package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.ChatNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.ClientCredentialsRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;
import scrapper.bot.connectivity.model.response.LinkResponse;

@ExtendWith(MockitoExtension.class)
public class ScrapperConnectionServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private LinkClient linkClient;

    @InjectMocks
    private ScrapperConnectionService scrapperConnectionService;

    private final Long chatId = 123L;
    private final String login = String.valueOf(chatId);
    private final String password = String.valueOf(chatId);

    @Test
    void registerChat_Success() throws BadRequestException {
        when(chatClient.registerChat(new ClientCredentialsRequest(login, password)))
                .thenReturn(ResponseEntity.ok().build());

        scrapperConnectionService.registerChat(chatId);

        verify(chatClient, times(1)).registerChat(new ClientCredentialsRequest(login, password));
    }

    @Test
    void registerChat_BadRequestException() {
        ApiErrorResponse errorResponse =
                new ApiErrorResponse("Invalid request", "400", "BadRequestException", "Bad request", List.of());
        doReturn(ResponseEntity.badRequest().body(errorResponse))
                .when(chatClient)
                .registerChat(new ClientCredentialsRequest(login, password));

        BadRequestException exception =
                assertThrows(BadRequestException.class, () -> scrapperConnectionService.registerChat(chatId));

        assertEquals(Messages.INVALID_MESSAGE.toString(), exception.getMessage());
        verify(chatClient, times(1)).registerChat(new ClientCredentialsRequest(login, password));
    }

    @Test
    void unregisterChat_Success() throws BadRequestException {
        when(chatClient.unregisterChat(new ClientCredentialsRequest(login, password)))
                .thenReturn(ResponseEntity.ok().build());

        scrapperConnectionService.unregisterChat(chatId);

        verify(chatClient, times(1)).unregisterChat(new ClientCredentialsRequest(login, password));
    }

    @Test
    void unregisterChat_BadRequestException() {
        ApiErrorResponse errorResponse =
                new ApiErrorResponse("Invalid request", "400", "BadRequestException", "Bad request", List.of());
        doReturn(ResponseEntity.badRequest().body(errorResponse))
                .when(chatClient)
                .unregisterChat(new ClientCredentialsRequest(login, password));

        BadRequestException exception =
                assertThrows(BadRequestException.class, () -> scrapperConnectionService.unregisterChat(chatId));

        assertEquals(Messages.INVALID_MESSAGE.toString(), exception.getMessage());
        verify(chatClient, times(1)).unregisterChat(new ClientCredentialsRequest(login, password));
    }

    @Test
    void unregisterChat_NotFound() {
        ApiErrorResponse errorResponse =
                new ApiErrorResponse("Chat not found", "404", "ResourceNotFoundException", "Not found", List.of());
        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse))
                .when(chatClient)
                .unregisterChat(new ClientCredentialsRequest(login, password));

        ChatNotFoundException exception =
                assertThrows(ChatNotFoundException.class, () -> scrapperConnectionService.unregisterChat(chatId));

        assertEquals(Messages.ERROR.toString(), exception.getMessage());
        verify(chatClient, times(1)).unregisterChat(new ClientCredentialsRequest(login, password));
    }

    @Test
    void getAllLinks_Success() throws BadRequestException {
        List<LinkResponse> listLinkResponse =
                List.of(new LinkResponse(1L, URI.create("https://example.com"), Set.of(), Set.of()));
        doReturn(ResponseEntity.ok().body(listLinkResponse)).when(linkClient).getAllLinks(login, password);

        List<LinkResponse> result = scrapperConnectionService.getAllLinks(chatId);

        assertEquals(listLinkResponse.size(), result.size());
        assertEquals(listLinkResponse, result);
        verify(linkClient, times(1)).getAllLinks(login, password);
    }

    @Test
    void getAllLinks_BadRequestException() {
        ApiErrorResponse errorResponse =
                new ApiErrorResponse("Invalid request", "400", "BadRequestException", "Bad request", List.of());
        doReturn(ResponseEntity.badRequest().body(errorResponse))
                .when(linkClient)
                .getAllLinks(login, password);

        BadRequestException exception =
                assertThrows(BadRequestException.class, () -> scrapperConnectionService.getAllLinks(chatId));

        assertEquals(Messages.INVALID_MESSAGE.toString(), exception.getMessage());
        verify(linkClient, times(1)).getAllLinks(login, password);
    }

    @Test
    void unsubscribeLink_Success() {
        LinkResponse linkResponse = new LinkResponse(1L, URI.create("https://example.com"), Set.of(), Set.of());
        List<LinkResponse> subscribedLinks = List.of(linkResponse);
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("https://example.com"));

        doReturn(ResponseEntity.ok(linkResponse))
                .when(linkClient)
                .unsubscribeLink(login, password, removeLinkRequest);

        boolean result = scrapperConnectionService.unsubscribeLink(chatId, subscribedLinks, 1L);

        assertTrue(result);
        verify(linkClient, times(1)).unsubscribeLink(login, password, removeLinkRequest);
    }

    @Test
    void unsubscribeLink_BadRequestException() {
        LinkResponse linkResponse = new LinkResponse(1L, URI.create("https://example.com"), Set.of(), Set.of());
        List<LinkResponse> subscribedLinks = List.of(linkResponse);
        ApiErrorResponse errorResponse =
                new ApiErrorResponse("Invalid request", "400", "BadRequestException", "Bad request", List.of());
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("https://example.com"));

        doReturn(ResponseEntity.badRequest().body(errorResponse))
                .when(linkClient)
                .unsubscribeLink(login, password, removeLinkRequest);

        boolean result = scrapperConnectionService.unsubscribeLink(chatId, subscribedLinks, 1L);

        assertFalse(result);
        verify(linkClient, times(1)).unsubscribeLink(login, password, removeLinkRequest);
    }

    @Test
    void unsubscribeLink_LinkNotFound() {
        LinkResponse linkResponse = new LinkResponse(1L, URI.create("https://example.com"), Set.of(), Set.of());
        List<LinkResponse> subscribedLinks = List.of(linkResponse);
        ApiErrorResponse errorResponse =
                new ApiErrorResponse("Link not found", "404", "NotFoundException", "Not found", List.of());
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(URI.create("https://example.com"));

        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse))
                .when(linkClient)
                .unsubscribeLink(login, password, removeLinkRequest);

        boolean result = scrapperConnectionService.unsubscribeLink(chatId, subscribedLinks, 1L);

        assertFalse(result);
        verify(linkClient, times(1)).unsubscribeLink(login, password, removeLinkRequest);
    }
}
