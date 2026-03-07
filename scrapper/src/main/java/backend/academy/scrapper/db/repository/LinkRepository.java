package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.model.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

public interface LinkRepository {

    Long save(String link, OffsetDateTime createdTime);

    Optional<Link> findIdByLink(String link);

    Optional<Link> findById(Long linkId);

    boolean existsLink(String link);

    Optional<Link> delete(Long id);

    Set<URI> findAllLinksByUpdatedAt(OffsetDateTime highestTimeLimit, int offset, Integer limit);
}
