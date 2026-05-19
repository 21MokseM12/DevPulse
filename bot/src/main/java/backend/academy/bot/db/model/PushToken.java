package backend.academy.bot.db.model;

import java.time.OffsetDateTime;

public record PushToken(
        Long id,
        String clientLogin,
        PushPlatform platform,
        String token,
        String appVersion,
        String deviceId,
        PushTokenStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime lastSeenAt) {}
