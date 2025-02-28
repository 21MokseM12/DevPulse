package backend.academy.bot.client;

import scrapper.bot.connectivity.model.ScrapperResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/tg-chat/{id}")
public interface ChatClient {

    @PostExchange
    ResponseEntity<ScrapperResponse> registerChat(@PathVariable Long id);

    @DeleteExchange
    ResponseEntity<ScrapperResponse> unregisterChat(@PathVariable Long id);
}
