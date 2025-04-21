package backend.academy.scrapper.controller;

import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.validators.LinkValidatorManager;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LinkController {

    private final LinkService linkService;

    private final LinkValidatorManager linkValidatorManager;

    @Autowired
    public LinkController(LinkService linkService, LinkValidatorManager linkValidatorManager) {
        this.linkService = linkService;
        this.linkValidatorManager = linkValidatorManager;
    }

    @GetMapping
    public ResponseEntity<ListLinkResponse> findAll(@RequestHeader(name = "Tg-Chat-Id") Long chatId) {
        List<LinkResponse> allByChatId = linkService.findAllByChatId(chatId);
        return ResponseEntity.ok(new ListLinkResponse(allByChatId, allByChatId.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> subscribeLink(
            @RequestHeader(name = "Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest link)
            throws BadRequestException {
        if (!linkValidatorManager.isValidLink(link.link().toString())) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        Optional<LinkResponse> optionalLink = linkService.subscribe(chatId, link);
        if (optionalLink.isPresent()) {
            return ResponseEntity.ok(
                    optionalLink.orElseThrow(() -> new BadRequestException("Некорректные параметры запроса")));
        } else {
            throw new BadRequestException("Некорректные параметры запроса");
        }
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> unsubscribeLink(
            @RequestHeader(name = "Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest uri)
            throws ResourceNotFoundException, BadRequestException {
        if (!linkValidatorManager.isValidLink(uri.link().toString())) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        Optional<LinkResponse> optionalLink = linkService.unsubscribe(chatId, uri);
        if (optionalLink.isEmpty()) {
            throw new ResourceNotFoundException("Ссылка не найдена");
        }
        return ResponseEntity.ok(optionalLink.orElseThrow(() -> new ResourceNotFoundException("Ссылка не найдена")));
    }
}
