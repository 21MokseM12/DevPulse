package backend.academy.bot.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;

@HttpExchange("/links")
public interface LinkClient {

    public static final String CLIENT_LOGIN_HEADER = "Client-Login";

    @GetExchange
    ResponseEntity<?> getAllLinks(@RequestHeader(name = CLIENT_LOGIN_HEADER) String login);

    @PostExchange
    ResponseEntity<?> subscribeLink(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login,
            @RequestBody AddLinkRequest link);

    @DeleteExchange
    ResponseEntity<?> unsubscribeLink(
            @RequestHeader(name = CLIENT_LOGIN_HEADER) String login,
            @RequestBody RemoveLinkRequest uri);
}
