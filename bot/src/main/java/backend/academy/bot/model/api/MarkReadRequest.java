package backend.academy.bot.model.api;

import java.util.Set;

public record MarkReadRequest(Set<Long> ids, Boolean markAll) {}
