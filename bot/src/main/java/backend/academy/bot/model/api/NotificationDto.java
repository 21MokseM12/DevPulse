package backend.academy.bot.model.api;

import java.time.OffsetDateTime;

public record NotificationDto(
        Long id,
        Long linkId,
        String url,
        String title,
        String updateOwner,
        String description,
        OffsetDateTime creationDate,
        OffsetDateTime receivedAt,
        OffsetDateTime readAt,
        boolean unread) {}
