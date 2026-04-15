package backend.academy.bot.controller;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.api.BotApiMessageResponse;
import backend.academy.bot.model.entity.LinkDTO;
import backend.academy.bot.service.ScrapperConnectionService;
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
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.ClientCredentialsRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BotRestController {

    private static final String CLIENT_LOGIN_HEADER = "Client-Login";
    private static final String CLIENT_PASSWORD_HEADER = "Client-Password";

    private final ScrapperConnectionService scrapperConnectionService;

    @PostMapping("/clients")
    public ResponseEntity<BotApiMessageResponse> registerClient(@RequestBody ClientCredentialsRequest request)
            throws BadRequestException {
        scrapperConnectionService.registerChat(request.login(), request.password());
        return ResponseEntity.ok(new BotApiMessageResponse(Messages.WELCOME_MESSAGE.toString()));
    }

    @DeleteMapping("/clients")
    public ResponseEntity<BotApiMessageResponse> unregisterClient(@RequestBody ClientCredentialsRequest request)
            throws BadRequestException {
        scrapperConnectionService.unregisterChat(request.login(), request.password());
        return ResponseEntity.ok(new BotApiMessageResponse(Messages.DELETE_SUBSCRIBE_MESSAGE.toString()));
    }

    @GetMapping("/links")
    public ResponseEntity<List<LinkResponse>> getLinks(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login,
            @RequestHeader(name = CLIENT_PASSWORD_HEADER) String password) throws BadRequestException {
        return ResponseEntity.ok(scrapperConnectionService.getAllLinks(login, password));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> trackLink(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login,
            @RequestHeader(name = CLIENT_PASSWORD_HEADER) String password,
            @RequestBody AddLinkRequest request) throws BadRequestException {
        var link = new LinkDTO();
        link.uri(request.link().toString());
        link.tags(request.tags());
        link.filters(request.filters());
        return ResponseEntity.ok(scrapperConnectionService.subscribeLink(login, password, link));
    }

    @DeleteMapping("/links")
    public ResponseEntity<BotApiMessageResponse> untrackLink(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login,
            @RequestHeader(name = CLIENT_PASSWORD_HEADER) String password,
            @RequestBody RemoveLinkRequest request) throws BadRequestException {
        var links = scrapperConnectionService.getAllLinks(login, password);
        var deleted = links.stream()
            .filter(link -> link.url().equals(request.link()))
            .findFirst()
            .map(LinkResponse::id)
            .map(id -> scrapperConnectionService.unsubscribeLink(login, password, links, id))
            .orElse(false);
        if (!deleted) {
            throw new BadRequestException(Messages.ERROR.toString());
        }
        return ResponseEntity.ok(new BotApiMessageResponse(Messages.DELETE_SUBSCRIBE_MESSAGE.toString()));
    }
}
