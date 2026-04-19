package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.ChatOperationProcessor;
import backend.academy.scrapper.service.LinkProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private static final String CLIENT_LOGIN_REQUEST_HEADER = "Client-Login";

    private final ChatOperationProcessor chatProcessor;
    private final LinkProcessor processor;

    @GetMapping
    public ResponseEntity<List<LinkResponse>> findAll(
            @RequestHeader(name = CLIENT_LOGIN_REQUEST_HEADER) String login
    ) {
        Long chatId = resolveChatId(login);
        return ResponseEntity.ok(processor.findAll(chatId));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> subscribeLink(
            @RequestHeader(name = CLIENT_LOGIN_REQUEST_HEADER) String login,
            @RequestBody AddLinkRequest request
    ) {
        Long chatId = resolveChatId(login);
        return ResponseEntity.ok(processor.subscribeLink(chatId, request));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> unsubscribeLink(
            @RequestHeader(name = CLIENT_LOGIN_REQUEST_HEADER) String login,
            @RequestBody RemoveLinkRequest request
    ) {
        Long chatId = resolveChatId(login);
        return ResponseEntity.ok(processor.unsubscribeLink(chatId, request));
    }

    private Long resolveChatId(String login) {
        return chatProcessor.findClientIdByLogin(login)
            .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден"));
    }
}
