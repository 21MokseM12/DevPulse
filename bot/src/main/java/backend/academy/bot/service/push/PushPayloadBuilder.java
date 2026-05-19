package backend.academy.bot.service.push;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.model.LinkUpdate;

@Component
public class PushPayloadBuilder {

    public PushMessagePayload build(long eventId, LinkUpdate update) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("event_id", String.valueOf(eventId));
        metadata.put("title", update.title());
        metadata.put("content", update.description());
        if (update.eventUrl() != null) {
            metadata.put("url", update.eventUrl().toString());
        }
        metadata.put("created_at", update.creationDate().toString());
        metadata.put("source", update.updateOwner());
        metadata.put("link_id", String.valueOf(update.id()));
        return new PushMessagePayload(
                String.valueOf(eventId),
                update.title(),
                update.description(),
                update.eventUrl() == null ? null : update.eventUrl().toString(),
                update.creationDate().toString(),
                update.updateOwner(),
                metadata);
    }
}
