package backend.academy.bot.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/tg-chat/{id}")
public interface ChatClient {

    @PostMapping
    ResponseEntity<?> registerChat(@PathVariable Long id);

    @DeleteMapping
    ResponseEntity<?> unregisterChat(@PathVariable Long id);
}
