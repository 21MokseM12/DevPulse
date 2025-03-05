package backend.academy.scrapper.client;

import backend.academy.scrapper.model.StackOverflowResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import java.util.List;

@HttpExchange("/questions/{questionId}")
public interface StackOverflowClient {

    @GetExchange
    ResponseEntity<List<StackOverflowResponse>> getEvents(@PathVariable Long questionId);
}
