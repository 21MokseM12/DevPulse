package backend.academy.scrapper.db.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

public record Link(Long id, URI url, Set<String> tags, Set<String> filters, OffsetDateTime createdAt) {}
