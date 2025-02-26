package backend.academy.bot.service;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.model.LinkDTO;
import java.util.List;
import java.util.Objects;
import backend.academy.bot.utils.LinkDTOConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.ApiErrorResponse;
import scrapper.bot.connectivity.model.Link;

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
        ResponseEntity<?> response = chatClient.registerChat(chatId);
        switch (response.getStatusCode().value()) {
            case 200:
                log.info(response.getBody().toString());
                break;
            case 400:
                ApiErrorResponse error = (ApiErrorResponse) response.getBody();
                log.error("Error occur via register chat: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public void unregisterChat(Long chatId) throws BadRequestException {
        ResponseEntity<?> response = chatClient.unregisterChat(chatId);
        switch (response.getStatusCode().value()) {
            case 200:
                log.info(response.getBody().toString());
                break;
            case 400:
                ApiErrorResponse error400 = (ApiErrorResponse) response.getBody();
                log.error("Error occur via unregister chat: {}", error400);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            case 404:
                ApiErrorResponse error404 = (ApiErrorResponse) response.getBody();
                log.error("Chat not found: {}", error404);
                throw new ChatNotFoundException(Messages.ERROR.toString());
        }
    }

    public List<Link> getAllLinks(Long chatId) throws BadRequestException {
        ResponseEntity<?> response = linkClient.getAllLinks(chatId);
        switch (response.getStatusCode().value()) {
            case 200:
                return (List<Link>) response.getBody();
            case 400:
                ApiErrorResponse error = (ApiErrorResponse) response.getBody();
                log.error("Error occur via getAllLinks: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
        return List.of();
    }

    public void subscribeLink(Long chatId, LinkDTO linkDTO) throws BadRequestException {
        ResponseEntity<?> response = linkClient.subscribeLink(
            chatId,
            linkDTOConverter.toLinkRequest(linkDTO)
        );
        switch (response.getStatusCode().value()) {
            case 200:
                log.info(response.getBody().toString());
                break;
            case 400:
                ApiErrorResponse error = (ApiErrorResponse) response.getBody();
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
        ResponseEntity<?> response = linkClient.unsubscribeLink(chatId, uri);
        switch (response.getStatusCode().value()) {
            case 200:
                log.info(response.getBody().toString());
                return true;
            case 400:
                ApiErrorResponse error400 = (ApiErrorResponse) response.getBody();
                log.error("Error occur via unsubscribeLink: {}", error400);
                return false;
            case 404:
                ApiErrorResponse error404 = (ApiErrorResponse) response.getBody();
                log.error("Link was not found: {}", error404);
                return false;
        }
        return false;
    }
}
