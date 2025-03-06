package backend.academy.scrapper.client;

import backend.academy.scrapper.model.StackOverflowResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/questions/{questionId}")
public interface StackOverflowClient {

    @GetExchange
    ResponseEntity<StackOverflowResponse> getEvents(
        @PathVariable Long questionId,
        @RequestParam String order,
        @RequestParam String sort,
        @RequestParam String site);
}
