package backend.academy.bot.service;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.model.entity.LinkDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.ClientCredentialsRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Service
@Slf4j
public class ScrapperConnectionService {

    private final ChatClient chatClient;

    private final LinkClient linkClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public ScrapperConnectionService(ChatClient chatClient, LinkClient linkClient) {
        this.chatClient = chatClient;
        this.linkClient = linkClient;
    }

    public void registerChat(String login, String password) throws BadRequestException {
        ResponseEntity<?> response;
        try {
            response = chatClient.registerChat(new ClientCredentialsRequest(login, password));
        } catch (Exception e) {
            log.error("Error occur via register chat with login {}: {}", login, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
        switch (response.getStatusCode().value()) {
            case 200:
                log.info("Chat with login {} registered successfully", login);
                break;
            case 400:
                ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Error occur via register chat: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            default:
                throw new BadRequestException(Messages.ERROR.toString());
        }
    }

    public void registerChat(Long chatId) throws BadRequestException {
        registerChat(String.valueOf(chatId), String.valueOf(chatId));
    }

    public void unregisterChat(String login, String password) throws BadRequestException {
        ResponseEntity<?> response;
        try {
            response = chatClient.unregisterChat(new ClientCredentialsRequest(login, password));
        } catch (Exception e) {
            log.error("Error occur via unregister chat with login {}: {}", login, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
        switch (response.getStatusCode().value()) {
            case 200:
                log.info("Chat with login {} unregistered successfully", login);
                break;
            case 400:
                ApiErrorResponse error400 = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Error occur via unregister chat: {}", error400);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            case 404:
                ApiErrorResponse error404 = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Chat not found: {}", error404);
                throw new ChatNotFoundException(Messages.ERROR.toString());
            default:
                throw new BadRequestException(Messages.ERROR.toString());
        }
    }

    public void unregisterChat(Long chatId) throws BadRequestException {
        unregisterChat(String.valueOf(chatId), String.valueOf(chatId));
    }

    public List<LinkResponse> getAllLinks(String login, String password) throws BadRequestException {
        ResponseEntity<?> response;
        try {
            response = linkClient.getAllLinks(login, password);
        } catch (Exception e) {
            log.error("Error occur via getting all links with login {}: {}", login, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
        switch (response.getStatusCode().value()) {
            case 200:
                return MAPPER.convertValue(
                    response.getBody(),
                    MAPPER.getTypeFactory().constructCollectionType(List.class, LinkResponse.class)
                );
            case 400:
            case 404:
                ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Error occur via getAllLinks: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            default:
                return List.of();
        }
    }

    public List<LinkResponse> getAllLinks(Long chatId) throws BadRequestException {
        return getAllLinks(String.valueOf(chatId), String.valueOf(chatId));
    }

    public LinkResponse subscribeLink(String login, String password, LinkDTO linkDTO) throws BadRequestException {
        ResponseEntity<?> response;
        try {
            response = linkClient.subscribeLink(
                    login, password, new AddLinkRequest(URI.create(linkDTO.uri()), linkDTO.tags(), linkDTO.filters()));
        } catch (Exception e) {
            log.error("Error occur via subscribe login {} on link: {}", login, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
        switch (response.getStatusCode().value()) {
            case 200:
                LinkResponse linkResponse = MAPPER.convertValue(response.getBody(), LinkResponse.class);
                log.info("Link was subscribed: {}", Objects.requireNonNull(linkResponse).url());
                return linkResponse;
            case 400:
            case 404:
                ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Error occur via subscribeLink: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            default:
                throw new BadRequestException(Messages.ERROR.toString());
        }
    }

    public boolean unsubscribeLink(String login, String password, List<LinkResponse> subscribedLinks, Long linkId) {
        URI uri = subscribedLinks.stream()
                .filter(l -> Objects.equals(l.id(), linkId))
                .findFirst()
                .orElseThrow()
                .url();
        ResponseEntity<?> response;
        try {
            response = linkClient.unsubscribeLink(login, password, new RemoveLinkRequest(uri));
        } catch (Exception e) {
            log.error("Error occur via unsubscribe login {} on link: {}", login, e.getMessage());
            return false;
        }
        switch (response.getStatusCode().value()) {
            case 200:
                LinkResponse linkResponse = MAPPER.convertValue(response.getBody(), LinkResponse.class);
                log.info("Link was unsubscribed: {}", Objects.requireNonNull(linkResponse).url());
                return true;
            case 400:
            case 404:
                ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Error occur via unsubscribeLink: {}", error);
                return false;
            default:
                return false;
        }
    }

    public boolean unsubscribeLink(Long chatId, List<LinkResponse> subscribedLinks, Long linkId) {
        return unsubscribeLink(String.valueOf(chatId), String.valueOf(chatId), subscribedLinks, linkId);
    }
}
