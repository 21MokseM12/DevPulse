package backend.academy.scrapper.db;

import backend.academy.scrapper.db.model.Link;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DbLinkService {
    Link saveLink(AddLinkRequest request);

    Optional<Long> findIdByLink(String link);

    Optional<Link> findById(Long id);

    boolean existsLink(String link);

    Optional<Link> delete(String link);

    List<Link> findAllLinks(List<Long> linkIds);

    Set<URI> findAllLinksByUpdatedAt(OffsetDateTime highestTimeLimit, int offsetMultiplier, Integer limit);


}
