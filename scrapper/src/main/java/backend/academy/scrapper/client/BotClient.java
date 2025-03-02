package backend.academy.scrapper.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import scrapper.bot.connectivity.model.LinkUpdate;

@HttpExchange("/updates")
public interface BotClient {

    @PostExchange
    ResponseEntity<?> sendUpdates(@RequestBody LinkUpdate linkUpdate);
}
