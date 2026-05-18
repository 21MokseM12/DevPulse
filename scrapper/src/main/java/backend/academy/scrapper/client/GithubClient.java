package backend.academy.scrapper.client;

import backend.academy.scrapper.model.github.GithubCompareResponse;
import backend.academy.scrapper.model.github.GithubResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/repos/{name}/{repo}")
public interface GithubClient {

    @GetExchange("/events")
    ResponseEntity<List<GithubResponse>> getEvents(
            @PathVariable String name,
            @PathVariable String repo,
            @RequestHeader(name = "If-None-Match", required = false) String ifNoneMatch,
            @RequestHeader(name = "If-Modified-Since", required = false) String ifModifiedSince);

    @GetExchange("/compare/{basehead}")
    ResponseEntity<GithubCompareResponse> compareCommits(
            @PathVariable String name, @PathVariable String repo, @PathVariable String basehead);
}
