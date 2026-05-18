package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubCompareCommitDetails(@JsonProperty("message") String message) {}
