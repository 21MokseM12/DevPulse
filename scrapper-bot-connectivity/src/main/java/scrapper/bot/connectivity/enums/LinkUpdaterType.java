package scrapper.bot.connectivity.enums;

import lombok.Getter;

@Getter
public enum LinkUpdaterType {
    GITHUB("github.com"),
    STACK_OVERFLOW("stackoverflow.com"),
    NONE("none");

    private final String domain;

    LinkUpdaterType(String domain) {
        this.domain = domain;
    }

    public static LinkUpdaterType fromLink(String link) {
        String domain = link.replace("//", "/").split("/")[1];
        for (LinkUpdaterType type : LinkUpdaterType.values()) {
            if (type.domain.equals(domain)) {
                return type;
            }
        }
        return NONE;
    }
}
