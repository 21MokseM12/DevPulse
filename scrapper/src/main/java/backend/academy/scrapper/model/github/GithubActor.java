package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubActor(
    @JsonProperty("login") String login
) {
}
