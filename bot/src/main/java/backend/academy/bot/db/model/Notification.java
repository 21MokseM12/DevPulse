package backend.academy.bot.db.model;

import java.time.OffsetDateTime;
import java.util.List;

public record Notification(
        Long linkId,
        String url,
        String title,
        String updateOwner,
        String description,
        OffsetDateTime creationDate,
        List<Long> clientsIds) {}
