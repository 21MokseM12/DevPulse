package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.model.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

public interface LinkRepository {

    Long save(String url, OffsetDateTime createdTime);

    Optional<Link> findIdByLink(String url);

    Optional<Link> findById(Long linkId);

    boolean existsLink(String url);

    Optional<Link> delete(Long id);

    Set<URI> findAllLinksByUpdatedAt(OffsetDateTime highestTimeLimit, int offset, Integer limit);

    Set<URI> findAllLinksForPolling(OffsetDateTime now, int offset, Integer limit);

    Optional<String> findEtagByLink(String url);

    void updateEtag(String url, String etag);

    Optional<OffsetDateTime> findLastModifiedByLink(String url);

    void updateLastModified(String url, OffsetDateTime lastModified);

    Optional<OffsetDateTime> findLastEventDateByLink(String url);

    void updateLastEventDate(String url, OffsetDateTime lastEventDate);

    void markPollingSuccess(String url, OffsetDateTime checkedAt, OffsetDateTime nextPollAt);

    void markPollingFailure(
            String url, OffsetDateTime checkedAt, String error, long baseBackoffSeconds, long maxBackoffSeconds);
}
