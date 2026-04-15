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

    @GetExchange
    ResponseEntity<?> getAllLinks(
            @RequestHeader(name = "Client-Login") String login,
            @RequestHeader(name = "Client-Password") String password);

    @PostExchange
    ResponseEntity<?> subscribeLink(
            @RequestHeader(name = "Client-Login") String login,
            @RequestHeader(name = "Client-Password") String password,
            @RequestBody AddLinkRequest link);

    @DeleteExchange
    ResponseEntity<?> unsubscribeLink(
            @RequestHeader(name = "Client-Login") String login,
            @RequestHeader(name = "Client-Password") String password,
            @RequestBody RemoveLinkRequest uri);
}
