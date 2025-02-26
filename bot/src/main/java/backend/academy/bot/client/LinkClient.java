package backend.academy.bot.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import scrapper.bot.connectivity.model.LinkRequest;

@HttpExchange("/links")
public interface LinkClient {

    @GetMapping
    ResponseEntity<?> getAllLinks(@RequestHeader(name = "Tg-Chat-Id") Long chatId);

    @PostMapping
    ResponseEntity<?> subscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody LinkRequest link
    );

    @DeleteMapping
    ResponseEntity<?> unsubscribeLink(
        @RequestHeader(name = "Tg-Chat-Id") Long chatId,
        @RequestBody String uri
    );
}
