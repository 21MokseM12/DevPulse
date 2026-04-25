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
}
