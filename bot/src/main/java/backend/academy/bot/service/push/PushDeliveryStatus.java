package backend.academy.bot.service.push;

public enum PushDeliveryStatus {
    SUCCESS,
    TRANSIENT_ERROR,
    INVALID_TOKEN,
    PERMANENT_ERROR
}
