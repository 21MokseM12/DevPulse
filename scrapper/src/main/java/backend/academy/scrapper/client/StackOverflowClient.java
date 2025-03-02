package backend.academy.scrapper.client;

import backend.academy.scrapper.model.StackOverflowResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/questions/{questionId}")
public interface StackOverflowClient {

    @GetExchange
    ResponseEntity<StackOverflowResponseDTO> getEvents(@PathVariable Long questionId);
}
