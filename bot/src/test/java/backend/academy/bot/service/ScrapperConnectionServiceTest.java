package backend.academy.bot.service;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.model.LinkDTO;
import backend.academy.bot.utils.LinkDTOConverter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.ApiErrorResponse;
import scrapper.bot.connectivity.model.Link;
import scrapper.bot.connectivity.model.LinkRequest;
import scrapper.bot.connectivity.model.ScrapperResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScrapperConnectionServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private LinkClient linkClient;

    @Mock
    private LinkDTOConverter linkDTOConverter;

    @InjectMocks
    private ScrapperConnectionService scrapperConnectionService;

    private final Long chatId = 123L;

    @Test
    void registerChat_Success() throws BadRequestException {
        ScrapperResponse mockResponse = new ScrapperResponse("Chat registered successfully");
        when(chatClient.registerChat(chatId)).thenReturn(ResponseEntity.ok(mockResponse));

        scrapperConnectionService.registerChat(chatId);

        verify(chatClient, times(1)).registerChat(chatId);
    }

    @Test
    void registerChat_BadRequestException() {
        ApiErrorResponse errorResponse = new ApiErrorResponse("Invalid request");
        ScrapperResponse mockResponse = new ScrapperResponse(errorResponse);
        when(chatClient.registerChat(chatId)).thenReturn(ResponseEntity.badRequest().body(mockResponse));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            scrapperConnectionService.registerChat(chatId)
        );

        assertEquals(Messages.INVALID_MESSAGE.toString(), exception.getMessage());
        verify(chatClient, times(1)).registerChat(chatId);
    }

    @Test
    void unregisterChat_Success() throws BadRequestException {
        ScrapperResponse mockResponse = new ScrapperResponse("Chat unregistered successfully");
        when(chatClient.unregisterChat(chatId)).thenReturn(ResponseEntity.ok(mockResponse));

        scrapperConnectionService.unregisterChat(chatId);

        verify(chatClient, times(1)).unregisterChat(chatId);
    }

    @Test
    void unregisterChat_BadRequestException() {
        ApiErrorResponse errorResponse = new ApiErrorResponse("Bad request");
        ScrapperResponse mockResponse = new ScrapperResponse(errorResponse);
        when(chatClient.unregisterChat(chatId)).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mockResponse));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            scrapperConnectionService.unregisterChat(chatId)
        );

        assertEquals(Messages.INVALID_MESSAGE.toString(), exception.getMessage());
        verify(chatClient, times(1)).unregisterChat(chatId);
    }

    @Test
    void unregisterChat_NotFound() {
        ApiErrorResponse errorResponse = new ApiErrorResponse("Chat not found");
        ScrapperResponse mockResponse = new ScrapperResponse(errorResponse);
        when(chatClient.unregisterChat(chatId)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(mockResponse));

        ChatNotFoundException exception = assertThrows(ChatNotFoundException.class, () ->
            scrapperConnectionService.unregisterChat(chatId)
        );

        assertEquals(Messages.ERROR.toString(), exception.getMessage());
        verify(chatClient, times(1)).unregisterChat(chatId);
    }

    @Test
    void getAllLinks_Success() throws BadRequestException {
        List<Link> links = List.of(new Link(1, "https://example.com"));
        ScrapperResponse mockResponse = new ScrapperResponse(links);
        when(linkClient.getAllLinks(chatId)).thenReturn(ResponseEntity.ok(mockResponse));

        List<Link> result = scrapperConnectionService.getAllLinks(chatId);

        assertEquals(links, result);
        verify(linkClient, times(1)).getAllLinks(chatId);
    }

    @Test
    void getAllLinks_BadRequestException() {
        ApiErrorResponse errorResponse = new ApiErrorResponse("Invalid request");
        ScrapperResponse mockResponse = new ScrapperResponse(errorResponse);
        when(linkClient.getAllLinks(chatId)).thenReturn(ResponseEntity.badRequest().body(mockResponse));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            scrapperConnectionService.getAllLinks(chatId)
        );

        assertEquals(Messages.INVALID_MESSAGE.toString(), exception.getMessage());
        verify(linkClient, times(1)).getAllLinks(chatId);
    }

    @Test
    void subscribeLink_Success() throws BadRequestException {
        LinkDTO linkDTO = new LinkDTO("https://example.com");
        ScrapperResponse mockResponse = new ScrapperResponse("Subscribed successfully");

        when(linkDTOConverter.toLinkRequest(linkDTO)).thenReturn(new LinkRequest("ConvertedLinkRequest"));
        when(linkClient.subscribeLink(chatId, new LinkRequest("ConvertedLinkRequest"))).thenReturn(ResponseEntity.ok(mockResponse));

        scrapperConnectionService.subscribeLink(chatId, linkDTO);

        verify(linkClient, times(1)).subscribeLink(chatId, new LinkRequest("ConvertedLinkRequest"));
    }

    @Test
    void subscribeLink_BadRequestException() {
        LinkRequest linkRequest = new LinkRequest("https://example.com");
        LinkDTO linkDTO = new LinkDTO("https://example.com");
        ApiErrorResponse errorResponse = new ApiErrorResponse("Bad request");
        ScrapperResponse mockResponse = new ScrapperResponse(errorResponse);

        when(linkDTOConverter.toLinkRequest(linkDTO)).thenReturn(linkRequest);
        when(linkClient.subscribeLink(chatId, linkRequest)).thenReturn(ResponseEntity.badRequest().body(mockResponse));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            scrapperConnectionService.subscribeLink(chatId, linkDTO)
        );

        assertEquals(Messages.INVALID_MESSAGE.toString(), exception.getMessage());
        verify(linkClient, times(1)).subscribeLink(chatId, linkRequest);
    }

    @Test
    void unsubscribeLink_Success() {
        List<Link> subscribedLinks = List.of(new Link(1, "https://example.com"));
        ScrapperResponse mockResponse = new ScrapperResponse("Unsubscribed successfully");

        when(linkClient.unsubscribeLink(chatId, "https://example.com")).thenReturn(ResponseEntity.ok(mockResponse));

        boolean result = scrapperConnectionService.unsubscribeLink(chatId, subscribedLinks, 1);

        assertTrue(result);
        verify(linkClient, times(1)).unsubscribeLink(chatId, "https://example.com");
    }

    @Test
    void unsubscribeLink_BadRequestException() {
        List<Link> subscribedLinks = List.of(new Link(1, "https://example.com"));
        ApiErrorResponse errorResponse = new ApiErrorResponse("Bad request");
        ScrapperResponse mockResponse = new ScrapperResponse(errorResponse);

        when(linkClient.unsubscribeLink(chatId, "https://example.com")).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mockResponse));

        boolean result = scrapperConnectionService.unsubscribeLink(chatId, subscribedLinks, 1);

        assertFalse(result);
        verify(linkClient, times(1)).unsubscribeLink(chatId, "https://example.com");
    }

    @Test
    void unsubscribeLink_LinkNotFound() {
        List<Link> subscribedLinks = List.of(new Link(1, "https://example.com"));
        ApiErrorResponse errorResponse = new ApiErrorResponse("Link not found");
        ScrapperResponse mockResponse = new ScrapperResponse(errorResponse);

        when(linkClient.unsubscribeLink(chatId, "https://example.com")).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(mockResponse));

        boolean result = scrapperConnectionService.unsubscribeLink(chatId, subscribedLinks, 1);

        assertFalse(result);
        verify(linkClient, times(1)).unsubscribeLink(chatId, "https://example.com");
    }
}
