package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.validators.LinkValidatorManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;
import scrapper.bot.connectivity.model.response.ListLinkResponse;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private static final String CHAT_ID_REQUEST_HEADER = "Tg-Chat-Id";

    private final LinkOperationProcessor linkOperationProcessor;
    private final LinkValidatorManager linkValidatorManager;

    @GetMapping
    public ResponseEntity<ListLinkResponse> findAll(@RequestHeader(name = CHAT_ID_REQUEST_HEADER) Long chatId) {
        List<LinkResponse> allByChatId = linkOperationProcessor.findAllByChatId(chatId);
        return ResponseEntity.ok(new ListLinkResponse(allByChatId, allByChatId.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> subscribeLink(
            @RequestHeader(name = CHAT_ID_REQUEST_HEADER) Long chatId, @RequestBody AddLinkRequest link)
            throws BadRequestException {
        if (!linkValidatorManager.isValidLink(link.link().toString())) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        Optional<LinkResponse> optionalLink = linkOperationProcessor.subscribe(chatId, link);
        if (optionalLink.isPresent()) {
            return ResponseEntity.ok(
                    optionalLink.orElseThrow(() -> new BadRequestException("Некорректные параметры запроса")));
        } else {
            throw new BadRequestException("Некорректные параметры запроса");
        }
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> unsubscribeLink(
            @RequestHeader(name = CHAT_ID_REQUEST_HEADER) Long chatId, @RequestBody RemoveLinkRequest uri)
            throws ResourceNotFoundException, BadRequestException {
        if (!linkValidatorManager.isValidLink(uri.link().toString())) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        Optional<LinkResponse> optionalLink = linkOperationProcessor.unsubscribe(chatId, uri);
        if (optionalLink.isEmpty()) {
            throw new ResourceNotFoundException("Ссылка не найдена");
        }
        return ResponseEntity.ok(optionalLink.orElseThrow(() -> new ResourceNotFoundException("Ссылка не найдена")));
    }
}
