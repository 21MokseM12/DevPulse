package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.LinkService;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.service.validators.LinkValidatorManager;
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
import scrapper.bot.connectivity.model.response.LinkResponse;
import scrapper.bot.connectivity.model.response.ListLinkResponse;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;

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
    public ResponseEntity<ListLinkResponse> findAll(@RequestHeader(name = "Tg-Chat-Id") Long chatId)
    throws ResourceNotFoundException, BadRequestException {
        Optional<List<LinkResponse>> optionalLinks = linkService.findAllByChatId(chatId);
        if (optionalLinks.isPresent()) {
            List<LinkResponse> links = optionalLinks.get();
            return ResponseEntity.ok(new ListLinkResponse(
                links,
                links.size()
            ));
        } else {
            throw new BadRequestException("Некорректные параметры запроса");
        }
    }

    @PostMapping
    public ResponseEntity<LinkResponse> subscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody AddLinkRequest link
    ) throws BadRequestException {
        if (!linkValidatorManager.isValidLink(link.link().toString())) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        Optional<LinkResponse> optionalLink = linkService.subscribe(chatId, link);
        if (optionalLink.isPresent()) {
            return ResponseEntity.ok(optionalLink.get());
        } else {
            throw new BadRequestException("Некорректные параметры запроса");
        }
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> unsubscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody RemoveLinkRequest uri
    ) throws ResourceNotFoundException, BadRequestException {
        if (!linkValidatorManager.isValidLink(uri.link().toString())) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        Optional<LinkResponse> optionalLink = linkService.unsubscribe(chatId, uri);
        if (optionalLink.isEmpty()) {
            throw new ResourceNotFoundException("Ссылка не найдена");
        }
        return ResponseEntity.ok(optionalLink.get());
    }
}
