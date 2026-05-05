package backend.academy.scrapper.db.model;

import java.util.Set;

public record LinkSubscription(Long clientId, Set<String> filters) {}
