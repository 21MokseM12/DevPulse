package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubIssue(
    @JsonProperty("title") String title,
    @JsonProperty("body") String body
) {
}
