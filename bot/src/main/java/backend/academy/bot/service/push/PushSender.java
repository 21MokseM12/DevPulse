package backend.academy.bot.service.push;

public interface PushSender {
    PushDeliveryResult send(String token, PushMessagePayload payload);
}
