package backend.academy.bot.db.model;

public enum PushPlatform {
    ANDROID;

    public String dbValue() {
        return name().toLowerCase();
    }
}
