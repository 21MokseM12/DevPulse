package backend.academy.bot.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import scrapper.bot.connectivity.model.connectivity.AddLinkRequest;
import scrapper.bot.connectivity.model.connectivity.RemoveLinkRequest;

@HttpExchange("/links")
public interface LinkClient {

    @GetExchange
    ResponseEntity<?> getAllLinks(@RequestHeader(name = "Tg-Chat-Id") Long chatId);

    @PostExchange
    ResponseEntity<?> subscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody AddLinkRequest link
    );

    @DeleteExchange
    ResponseEntity<?> unsubscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody RemoveLinkRequest uri
    );
}
