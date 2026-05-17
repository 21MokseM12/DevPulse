package backend.academy.scrapper.enums;

import lombok.Getter;

@Getter
public enum GithubActionType {
    PULL_REQUEST_EVENT("PullRequestEvent"),
    ISSUE_EVENT("IssuesEvent"),
    PUSH_EVENT("PushEvent");

    private final String type;

    GithubActionType(String type) {
        this.type = type;
    }
}
