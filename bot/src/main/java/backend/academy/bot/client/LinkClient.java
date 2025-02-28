package backend.academy.bot.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import scrapper.bot.connectivity.model.LinkRequest;
import scrapper.bot.connectivity.model.ScrapperResponse;

@HttpExchange("/links")
public interface LinkClient {

    @GetExchange
    ResponseEntity<ScrapperResponse> getAllLinks(@RequestHeader(name = "Tg-Chat-Id") Long chatId);

    @PostExchange
    ResponseEntity<ScrapperResponse> subscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody LinkRequest link
    );

    @DeleteExchange
    ResponseEntity<ScrapperResponse> unsubscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody String uri
    );
}
