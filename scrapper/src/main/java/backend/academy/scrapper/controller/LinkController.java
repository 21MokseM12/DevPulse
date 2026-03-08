package backend.academy.scrapper.controller;

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

    private static final String CHAT_ID_REQUEST_HEADER = "Tg-Chat-Id";

    private final LinkProcessor processor;

    @GetMapping
    public ResponseEntity<List<LinkResponse>> findAll(@RequestHeader(name = CHAT_ID_REQUEST_HEADER) Long chatId) {
        return ResponseEntity.ok(processor.findAll(chatId));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> subscribeLink(
            @RequestHeader(name = CHAT_ID_REQUEST_HEADER) Long chatId,
            @RequestBody AddLinkRequest request
    ) {
        return ResponseEntity.ok(processor.subscribeLink(chatId, request));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> unsubscribeLink(
            @RequestHeader(name = CHAT_ID_REQUEST_HEADER) Long chatId,
            @RequestBody RemoveLinkRequest request
    ) {
        return ResponseEntity.ok(processor.unsubscribeLink(chatId, request));
    }
}
