package backend.academy.bot.service;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.client.LinkClient;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.model.LinkDTO;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.response.ApiErrorResponse;
import scrapper.bot.connectivity.model.response.LinkResponse;
import scrapper.bot.connectivity.model.response.ListLinkResponse;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;

@Service
@Slf4j
public class ScrapperConnectionService {

    private final ChatClient chatClient;

    private final LinkClient linkClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public ScrapperConnectionService(
        ChatClient chatClient,
        LinkClient linkClient
    ) {
        this.chatClient = chatClient;
        this.linkClient = linkClient;
    }

    public void registerChat(Long chatId) throws BadRequestException {
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
    }

    public void unregisterChat(Long chatId) throws BadRequestException {
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
    }

    public List<LinkResponse> getAllLinks(Long chatId) throws BadRequestException {
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
    }

    public void subscribeLink(Long chatId, LinkDTO linkDTO) throws BadRequestException {
        ResponseEntity<?> response = linkClient.subscribeLink(
            chatId,
            new AddLinkRequest(
                linkDTO.uri(),
                linkDTO.tags(),
                linkDTO.filters()
            )
        );
        switch (response.getStatusCode().value()) {
            case 200:
                LinkResponse linkResponse = MAPPER.convertValue(response.getBody(), LinkResponse.class);
                String message = "Link was subscribed: "
                    .concat(
                        Objects.requireNonNull(linkResponse).url()
                    );
                log.info(message);
                break;
            case 400:
                ApiErrorResponse error = MAPPER.convertValue(response.getBody(), ApiErrorResponse.class);
                log.error("Error occur via subscribeLink: {}", error);
                throw new BadRequestException(Messages.INVALID_MESSAGE.toString());
        }
    }

    public boolean unsubscribeLink(Long chatId, List<LinkResponse> subscribedLinks, Integer linkId) {
        String uri = subscribedLinks.stream()
            .filter(l -> Objects.equals(l.id(), (long) linkId))
            .findFirst()
            .get()
            .url();
        ResponseEntity<?> response = linkClient.unsubscribeLink(chatId, new RemoveLinkRequest(uri));
        switch (response.getStatusCode().value()) {
            case 200:
                LinkResponse linkResponse = MAPPER.convertValue(response.getBody(), LinkResponse.class);
                String message = "Link was unsubscribed: "
                    .concat(Objects.requireNonNull(linkResponse).url());
                log.info(message);
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
        return false;
    }
}
