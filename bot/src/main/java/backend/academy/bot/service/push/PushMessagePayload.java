package backend.academy.bot.service.push;

import java.util.Map;

public record PushMessagePayload(
        String eventId,
        String title,
        String content,
        String url,
        String createdAt,
        String source,
        Map<String, String> metadata) {}
