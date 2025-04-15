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
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;
import scrapper.bot.connectivity.model.response.LinkResponse;
import scrapper.bot.connectivity.model.response.ListLinkResponse;

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

    public void registerChat(Long chatId) throws BadRequestException {
        try {
            ResponseEntity<?> response = chatClient.registerChat(chatId);
            switch (response.getStatusCode().value()) {
                case 200:
                    log.info("Chat with id {} registered successfully", chatId);
                    break;
                case 400:
                    ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                    log.error("Error occur via register chat: {}", error);
                    throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            }
        } catch (Exception e) {
            log.error("Error occur via register chat with id {}: {}", chatId, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public void unregisterChat(Long chatId) throws BadRequestException {
        try {
            ResponseEntity<?> response = chatClient.unregisterChat(chatId);
            switch (response.getStatusCode().value()) {
                case 200:
                    log.info("Chat with id {} unregistered successfully", chatId);
                    break;
                case 400:
                    ApiErrorResponse error400 = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                    log.error("Error occur via unregister chat: {}", error400);
                    throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
                case 404:
                    ApiErrorResponse error404 = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                    log.error("Chat not found: {}", error404);
                    throw new ChatNotFoundException(Messages.ERROR.toString());
            }
        } catch (Exception e) {
            log.error("Error occur via unregister chat with id {}: {}", chatId, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public List<LinkResponse> getAllLinks(Long chatId) throws BadRequestException {
        try {
            ResponseEntity<?> response = linkClient.getAllLinks(chatId);
            switch (response.getStatusCode().value()) {
                case 200:
                    ListLinkResponse linkResponse = MAPPER.convertValue(response.getBody(), ListLinkResponse.class);
                    return linkResponse.links();
                case 400:
                    ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                    log.error("Error occur via getAllLinks: {}", error);
                    throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
            }
            return List.of();
        }  catch (Exception e) {
            log.error("Error occur via getting all links with chat id {}: {}", chatId, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public LinkResponse subscribeLink(Long chatId, LinkDTO linkDTO) throws BadRequestException {
        try {
            ResponseEntity<?> response = linkClient.subscribeLink(
                chatId, new AddLinkRequest(URI.create(linkDTO.uri()), linkDTO.tags(), linkDTO.filters()));
            switch (response.getStatusCode().value()) {
                case 200:
                    LinkResponse linkResponse = MAPPER.convertValue(response.getBody(), LinkResponse.class);
                    log.info(
                        "Link was subscribed: {}",
                        Objects.requireNonNull(linkResponse).url().toString());
                    return linkResponse;
                case 400:
                    ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                    log.error("Error occur via subscribeLink: {}", error);
                    throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
                default:
                    throw new BadRequestException(Messages.ERROR.toString());
            }
        }  catch (Exception e) {
            log.error("Error occur via subscribe chat with id {} on link: {}", chatId, e.getMessage());
            throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public boolean unsubscribeLink(Long chatId, List<LinkResponse> subscribedLinks, Long linkId) {
        URI uri = subscribedLinks.stream()
                .filter(l -> Objects.equals(l.id(), linkId))
                .findFirst()
                .orElseThrow()
                .url();
         try {
             ResponseEntity<?> response = linkClient.unsubscribeLink(chatId, new RemoveLinkRequest(uri));
             switch (response.getStatusCode().value()) {
                 case 200:
                     LinkResponse linkResponse = MAPPER.convertValue(response.getBody(), LinkResponse.class);
                     log.info(
                         "Link was unsubscribed: {}",
                         Objects.requireNonNull(linkResponse).url().toString());
                     return true;
                 case 400:
                     ApiErrorResponse error400 = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                     log.error("Error occur via unsubscribeLink: {}", error400);
                     return false;
                 case 404:
                     ApiErrorResponse error404 = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                     log.error("Link was not found: {}", error404);
                     return false;
             }
         } catch (Exception e) {
             log.error("Error occur via unsubscribe chat with id {} on link: {}", chatId, e.getMessage());
             return false;
         }
        return false;
    }
}
