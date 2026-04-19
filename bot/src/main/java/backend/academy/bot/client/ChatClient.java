package backend.academy.bot.client;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import scrapper.bot.connectivity.model.request.ClientCredentialsRequest;

@HttpExchange("/clients")
public interface ChatClient {

    @PostExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> registerChat(@RequestBody ClientCredentialsRequest request);

    @DeleteExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> unregisterChat(@RequestBody ClientCredentialsRequest request);
}
