package backend.academy.bot.service;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.model.LinkDTO;
import backend.academy.bot.utils.LinkDTOConverter;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.ApiErrorResponse;
import scrapper.bot.connectivity.model.Link;
import scrapper.bot.connectivity.model.ScrapperResponse;

@Service
@Slf4j
public class ScrapperConnectionService {

    private final ChatClient chatClient;

    private final LinkClient linkClient;

    private final LinkDTOConverter linkDTOConverter;

    @Autowired
    public ScrapperConnectionService(
        ChatClient chatClient,
        LinkClient linkClient,
        LinkDTOConverter linkDTOConverter
    ) {
        this.chatClient = chatClient;
        this.linkClient = linkClient;
        this.linkDTOConverter = linkDTOConverter;
    }

    public void registerChat(Long chatId) throws BadRequestException {
        ResponseEntity<ScrapperResponse> response = chatClient.registerChat(chatId);
        switch (response.getStatusCode().value()) {
            case 200:
                String message =
                    (String) Objects.requireNonNull(response.getBody()).response();
                log.info(message);
                break;
            case 400:
                ApiErrorResponse error =
                    (ApiErrorResponse) Objects.requireNonNull(response.getBody()).response();
                log.error("Error occur via register chat: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public void unregisterChat(Long chatId) throws BadRequestException {
        ResponseEntity<ScrapperResponse> response = chatClient.unregisterChat(chatId);
        switch (response.getStatusCode().value()) {
            case 200:
                String message =
                    (String) Objects.requireNonNull(response.getBody()).response();
                log.info(message);
                break;
            case 400:
                ApiErrorResponse error400 =
                    (ApiErrorResponse) Objects.requireNonNull(response.getBody()).response();
                log.error("Error occur via unregister chat: {}", error400);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            case 404:
                ApiErrorResponse error404 =
                    (ApiErrorResponse) Objects.requireNonNull(response.getBody()).response();
                log.error("Chat not found: {}", error404);
                throw new ChatNotFoundException(Messages.ERROR.toString());
        }
    }

    public List<Link> getAllLinks(Long chatId) throws BadRequestException {
        ResponseEntity<ScrapperResponse> response = linkClient.getAllLinks(chatId);
        switch (response.getStatusCode().value()) {
            case 200:
                return (List<Link>) response.getBody().response();
            case 400:
                ApiErrorResponse error =
                    (ApiErrorResponse) Objects.requireNonNull(response.getBody()).response();
                log.error("Error occur via getAllLinks: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
        return List.of();
    }

    public void subscribeLink(Long chatId, LinkDTO linkDTO) throws BadRequestException {
        ResponseEntity<ScrapperResponse> response = linkClient.subscribeLink(
            chatId,
            linkDTOConverter.toLinkRequest(linkDTO)
        );
        switch (response.getStatusCode().value()) {
            case 200:
                String message =
                    (String) Objects.requireNonNull(response.getBody()).response();
                log.info(message);
                break;
            case 400:
                ApiErrorResponse error =
                    (ApiErrorResponse) Objects.requireNonNull(response.getBody()).response();
                log.error("Error occur via subscribeLink: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public boolean unsubscribeLink(Long chatId, List<Link> subscribedLinks, Integer linkId) {
        String uri = subscribedLinks.stream()
            .filter(l -> Objects.equals(l.id(), linkId))
            .findFirst()
            .get()
            .uri();
        ResponseEntity<ScrapperResponse> response = linkClient.unsubscribeLink(chatId, uri);
        switch (response.getStatusCode().value()) {
            case 200:
                String message =
                    (String) Objects.requireNonNull(response.getBody()).response();
                log.info(message);
                return true;
            case 400:
                ApiErrorResponse error400 =
                    (ApiErrorResponse) Objects.requireNonNull(response.getBody()).response();
                log.error("Error occur via unsubscribeLink: {}", error400);
                return false;
            case 404:
                ApiErrorResponse error404 =
                    (ApiErrorResponse) Objects.requireNonNull(response.getBody()).response();
                log.error("Link was not found: {}", error404);
                return false;
        }
        return false;
    }
}
