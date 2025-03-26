package backend.academy.scrapper.enums;

import lombok.Getter;

@Getter
public enum ProcessedIdType {
    STACKOVERFLOW_ANSWER("stackoverflow_answer"),
    STACKOVERFLOW_COMMENT("stackoverflow_comment"),
    GITHUB_ISSUE("github_issue"),
    GITHUB_PULL_REQUEST("github_pull_request");

    private final String type;

    ProcessedIdType(String type) {
        this.type = type;
    }

    public static ProcessedIdType fromString(String type) {
        for (ProcessedIdType t : ProcessedIdType.values()) {
            if (t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }
}
