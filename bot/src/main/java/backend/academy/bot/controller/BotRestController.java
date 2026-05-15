package backend.academy.bot.controller;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.api.BotApiMessageResponse;
import backend.academy.bot.model.api.MarkReadRequest;
import backend.academy.bot.model.api.MarkReadResponse;
import backend.academy.bot.model.api.NotificationListResponse;
import backend.academy.bot.model.api.UnreadCountResponse;
import backend.academy.bot.model.entity.LinkDTO;
import backend.academy.bot.service.ClientOperationService;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.notifications.NotificationQueryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private final ClientOperationService clientOperationService;
    private final ScrapperConnectionService scrapperConnectionService;
    private final NotificationQueryService notificationQueryService;

    @PostMapping("/clients")
    public ResponseEntity<BotApiMessageResponse> registerClient(@Valid @RequestBody ClientCredentialsRequest request)
            throws BadRequestException {
        clientOperationService.registerClient(request.login(), request.password());
        return ResponseEntity.ok(new BotApiMessageResponse(Messages.WELCOME_MESSAGE.toString()));
    }

    @DeleteMapping("/clients")
    public ResponseEntity<BotApiMessageResponse> unregisterClient(@Valid @RequestBody ClientCredentialsRequest request)
            throws BadRequestException {
        clientOperationService.unregisterClient(request.login(), request.password());
        return ResponseEntity.ok(new BotApiMessageResponse(Messages.DELETE_SUBSCRIBE_MESSAGE.toString()));
    }

    @PostMapping("/clients/login")
    public ResponseEntity<BotApiMessageResponse> loginClient(@Valid @RequestBody ClientCredentialsRequest request)
            throws BadRequestException {
        clientOperationService.loginClient(request.login(), request.password());
        return ResponseEntity.ok(new BotApiMessageResponse(Messages.WELCOME_MESSAGE.toString()));
    }

    @GetMapping("/links")
    public ResponseEntity<List<LinkResponse>> getLinks(@RequestHeader(name = CLIENT_LOGIN_HEADER) String login)
            throws BadRequestException {
        return ResponseEntity.ok(scrapperConnectionService.getAllLinks(login));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> trackLink(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login, @Valid @RequestBody AddLinkRequest request)
            throws BadRequestException {
        var link = new LinkDTO();
        link.uri(request.link().toString());
        link.tags(request.tags());
        link.filters(request.filters());
        return ResponseEntity.ok(scrapperConnectionService.subscribeLink(login, link));
    }

    @DeleteMapping("/links")
    public ResponseEntity<BotApiMessageResponse> untrackLink(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login, @Valid @RequestBody RemoveLinkRequest request)
            throws BadRequestException {
        var links = scrapperConnectionService.getAllLinks(login);
        var deleted = links.stream()
                .filter(link -> link.url().equals(request.link()))
                .findFirst()
                .map(LinkResponse::id)
                .map(id -> scrapperConnectionService.unsubscribeLink(login, links, id))
                .orElse(false);
        if (!deleted) {
            throw new BadRequestException(Messages.ERROR.toString());
        }
        return ResponseEntity.ok(new BotApiMessageResponse(Messages.DELETE_SUBSCRIBE_MESSAGE.toString()));
    }

    @GetMapping("/notifications")
    public ResponseEntity<NotificationListResponse> getNotifications(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Set<String> tags)
            throws BadRequestException {
        return ResponseEntity.ok(notificationQueryService.list(login, limit, offset, tags));
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@RequestHeader(name = CLIENT_LOGIN_HEADER) String login) {
        return ResponseEntity.ok(notificationQueryService.unreadCount(login));
    }

    @PostMapping("/notifications/mark-read")
    public ResponseEntity<MarkReadResponse> markNotificationsRead(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login, @RequestBody MarkReadRequest request)
            throws BadRequestException {
        return ResponseEntity.ok(notificationQueryService.markRead(login, request));
    }
}
