package backend.academy.scrapper.db.repository;

import java.util.Set;

public interface TagRepository {

    void save(Set<String> tags, Long linkId);

    Set<String> findByLinkId(Long linkId);

    Set<String> deleteByLinkId(Long linkId);
}
