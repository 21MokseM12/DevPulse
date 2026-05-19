package backend.academy.bot.db.model;

import java.time.OffsetDateTime;

public record NotificationView(
        Long id,
        Long linkId,
        String url,
        String eventUrl,
        String title,
        String updateOwner,
        String description,
        OffsetDateTime creationDate,
        OffsetDateTime receivedAt,
        OffsetDateTime readAt) {}
