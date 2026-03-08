package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.validators.LinkValidatorManager;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LinkProcessorTest {

    @Mock
    private LinkOperationProcessor linkOperationProcessor;

    @Mock
    private LinkValidatorManager linkValidatorManager;

    @InjectMocks
    private LinkProcessorImpl linkProcessor;

    @Test
    public void testFindAllLinks_Successfully() {
        Long id = 123L;
        List<LinkResponse> expected = List.of(
            new LinkResponse(
                1L,
                URI.create("https://example.com"),
                Set.of(),
                Set.of()
            ),
            new LinkResponse(
                2L,
                URI.create("https://some-example.com"),
                Set.of(),
                Set.of()
            )
        );
        Mockito.when(linkOperationProcessor.findAllByChatId(id))
            .thenReturn(expected);

        List<LinkResponse> response = linkProcessor.findAll(id);

        assertEquals(expected, response);
    }

    @ParameterizedTest
    @MethodSource("findAllLinksInvalidData")
    public void testFindAllLinks_InvalidInput_ShouldReturnEmptyListLinkResponse(
        Long chatId,
        List<LinkResponse> expected
    ) {
        Mockito.when(linkOperationProcessor.findAllByChatId(chatId))
            .thenReturn(expected);

        List<LinkResponse> response = linkProcessor.findAll(chatId);

        assertEquals(expected, response);
    }

    @Test
    public void testSubscribeLink_Successfully() {
        Long id = 123L;
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://some-example.com"),
            Set.of("tag"),
            Set.of("filter")
        );
        LinkResponse expected = new LinkResponse(
            456L,
            request.link(),
            request.tags(),
            request.filters()
        );
        Mockito.when(linkValidatorManager.isValidLink(request.link().toString()))
            .thenReturn(true);
        Mockito.when(linkOperationProcessor.subscribe(id, request))
            .thenReturn(Optional.of(expected));

        LinkResponse response = linkProcessor.subscribeLink(id, request);

        assertEquals(expected, response);
    }

    @Test
    public void testSubscribeLink_InvalidLink_ShouldReturnBadRequestException() {
        Long id = 123L;
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://some-example.com"),
            Set.of("tag"),
            Set.of("filter")
        );
        String expectedMessage = "Некорректные параметры запроса";
        Mockito.when(linkValidatorManager.isValidLink(request.link().toString()))
            .thenReturn(false);

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> linkProcessor.subscribeLink(id, request)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testSubscribeLink_SubscribeFailed_ShouldReturnBadRequestException() {
        Long id = 123L;
        AddLinkRequest request = new AddLinkRequest(
            URI.create("https://some-example.com"),
            Set.of("tag"),
            Set.of("filter")
        );
        String expectedMessage = "Некорректные параметры запроса";
        Mockito.when(linkValidatorManager.isValidLink(request.link().toString()))
            .thenReturn(true);
        Mockito.when(linkOperationProcessor.subscribe(id, request))
            .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> linkProcessor.subscribeLink(id, request)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testUnsubscribeLink_Successfully() {
        Long id = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("https://some-example.com"));
        LinkResponse expected = new LinkResponse(
            456L,
            request.link(),
            Set.of("tag"),
            Set.of("filter")
        );
        Mockito.when(linkValidatorManager.isValidLink(request.link().toString()))
            .thenReturn(true);
        Mockito.when(linkOperationProcessor.unsubscribe(id, request))
            .thenReturn(Optional.of(expected));

        LinkResponse response = linkProcessor.unsubscribeLink(id, request);

        assertEquals(expected, response);
    }

    @Test
    public void testUnsubscribeLink_InvalidLink_ShouldReturnBadRequestException() {
        Long id = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("https://some-example.com"));
        String expectedMessage = "Некорректные параметры запроса";
        Mockito.when(linkValidatorManager.isValidLink(request.link().toString()))
            .thenReturn(false);

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> linkProcessor.unsubscribeLink(id, request)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testUnsubscribeLink_UnsubscribeFailed_ShouldReturnResourceNotFoundException() {
        Long id = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("https://some-example.com"));
        String expectedMessage = "Ссылка не найдена";
        Mockito.when(linkValidatorManager.isValidLink(request.link().toString()))
            .thenReturn(true);
        Mockito.when(linkOperationProcessor.unsubscribe(id, request))
            .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> linkProcessor.unsubscribeLink(id, request)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    public static Stream<Arguments> findAllLinksInvalidData() {
        return Stream.of(
            Arguments.of(123L, List.of()),
            Arguments.of(null, List.of())
        );
    }
}
