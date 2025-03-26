package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubPayload(
    @JsonProperty("action") String action,
    @JsonProperty("pull_request") GithubPullRequest pullRequest,
    @JsonProperty("issue") GithubIssue issue
) {
}
