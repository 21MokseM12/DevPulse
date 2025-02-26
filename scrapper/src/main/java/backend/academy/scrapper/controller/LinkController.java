package backend.academy.scrapper.controller;

import scrapper.bot.connectivity.exceptions.BadRequestException;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import scrapper.bot.connectivity.model.Link;
import scrapper.bot.connectivity.model.LinkRequest;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.validators.LinkValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/links")
public class LinkController {

    private final LinkService linkService;

    private final LinkValidator linkValidator;

    @Autowired
    public LinkController(LinkService linkService, LinkValidator linkValidator) {
        this.linkService = linkService;
        this.linkValidator = linkValidator;
    }

    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name = "Tg-Chat-Id") Long chatId)
    throws ResourceNotFoundException, BadRequestException {
        Optional<List<Link>> links = linkService.findAllByChatId(chatId);
        if (links.isPresent()) {
            return ResponseEntity.ok(links.get());
        } else {
            throw new BadRequestException("Некорректные параметры запроса");
        }
    }

    @PostMapping
    public ResponseEntity<?> subscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody LinkRequest link
    ) throws BadRequestException {
        Optional<Link> optionalLink = linkService.subscribe(chatId, link);
        if (optionalLink.isPresent()) {
            return ResponseEntity.ok(optionalLink.get());
        } else {
            throw new BadRequestException("Некорректные параметры запроса");
        }
    }

    @DeleteMapping
    public ResponseEntity<?> unsubscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody String uri
    ) throws ResourceNotFoundException, BadRequestException {
        if (!linkValidator.isValid(uri)) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        Optional<Link> optionalLink = linkService.unsubscribe(chatId, uri);
        if (optionalLink.isEmpty()) {
            throw new ResourceNotFoundException("Ссылка не найдена");
        }
        return ResponseEntity.ok(optionalLink.get());
    }
}
