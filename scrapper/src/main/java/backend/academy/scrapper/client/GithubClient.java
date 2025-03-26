package backend.academy.scrapper.client;

import backend.academy.scrapper.model.github.GithubResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/repos/{name}/{repo}/events")
public interface GithubClient {

    @GetExchange
    ResponseEntity<List<GithubResponse>> getEvents(@PathVariable String name, @PathVariable String repo);
}
