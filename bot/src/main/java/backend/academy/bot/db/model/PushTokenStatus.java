package backend.academy.bot.db.model;

public enum PushTokenStatus {
    ACTIVE,
    INACTIVE,
    INVALID;

    public String dbValue() {
        return name().toLowerCase();
    }
}
