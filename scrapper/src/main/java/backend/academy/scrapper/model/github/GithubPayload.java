package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GithubPayload(
        @JsonProperty("action") String action,
        @JsonProperty("pull_request") GithubPullRequest pullRequest,
        @JsonProperty("issue") GithubIssue issue,
        @JsonProperty("ref") String ref,
        @JsonProperty("head") String head,
        @JsonProperty("before") String before,
        @JsonProperty("commits") List<GithubCommit> commits) {
    public GithubPayload(String action, GithubPullRequest pullRequest, GithubIssue issue) {
        this(action, pullRequest, issue, null, null, null, List.of());
    }

    public GithubPayload(
            String action, GithubPullRequest pullRequest, GithubIssue issue, String ref, List<GithubCommit> commits) {
        this(action, pullRequest, issue, ref, null, null, commits);
    }
}
