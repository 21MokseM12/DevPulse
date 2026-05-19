package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GithubPullRequest(
        @JsonProperty("title") String title,
        @JsonProperty("body") String body,
        @JsonProperty("labels") List<GithubLabel> labels,
        @JsonProperty("html_url") String htmlUrl) {
    public GithubPullRequest(String title, String body, List<GithubLabel> labels) {
        this(title, body, labels, null);
    }
}
