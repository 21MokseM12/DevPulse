package backend.academy.bot.service.push;

public record PushDeliveryResult(PushDeliveryStatus status, String reason) {

    public static PushDeliveryResult success() {
        return new PushDeliveryResult(PushDeliveryStatus.SUCCESS, "ok");
    }

    public static PushDeliveryResult transientFailure(String reason) {
        return new PushDeliveryResult(PushDeliveryStatus.TRANSIENT_ERROR, reason);
    }

    public static PushDeliveryResult invalidToken(String reason) {
        return new PushDeliveryResult(PushDeliveryStatus.INVALID_TOKEN, reason);
    }

    public static PushDeliveryResult permanentFailure(String reason) {
        return new PushDeliveryResult(PushDeliveryStatus.PERMANENT_ERROR, reason);
    }
}
