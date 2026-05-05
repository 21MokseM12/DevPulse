package backend.academy.scrapper.db;

import backend.academy.scrapper.db.model.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import scrapper.bot.connectivity.model.request.AddLinkRequest;

public interface DbLinkService {
    Link saveLink(AddLinkRequest request);

    Optional<Link> findByLink(String link);

    Optional<Link> findById(Long id);

    boolean existsLink(String link);

    Optional<Link> delete(String link);

    List<Link> findAllLinks(List<Long> linkIds);

    Set<URI> findAllLinksByUpdatedAt(OffsetDateTime highestTimeLimit, int offsetMultiplier, Integer limit);

    Set<URI> findAllLinksForPolling(OffsetDateTime now, int offsetMultiplier, Integer limit);

    void markPollingSuccess(URI link, OffsetDateTime checkedAt, OffsetDateTime nextPollAt);

    void markPollingFailure(
            URI link, OffsetDateTime checkedAt, String error, long baseBackoffSeconds, long maxBackoffSeconds);
}
