package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubCommit(@JsonProperty("sha") String sha, @JsonProperty("message") String message) {}
