package backend.academy.bot.client;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/tg-chat/{id}")
public interface ChatClient {

    @PostExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> registerChat(@PathVariable Long id);

    @DeleteExchange
    ResponseEntity<?> unregisterChat(@PathVariable Long id);
}
