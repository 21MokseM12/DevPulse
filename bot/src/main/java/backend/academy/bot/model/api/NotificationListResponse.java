package backend.academy.bot.model.api;

import java.util.List;

public record NotificationListResponse(List<NotificationDto> notifications, int limit, int offset) {}
