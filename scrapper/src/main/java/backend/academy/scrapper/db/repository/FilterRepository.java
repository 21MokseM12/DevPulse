package backend.academy.scrapper.db.repository;

import java.util.Set;

public interface FilterRepository {

    void save(Set<String> filters, Long linkId);

    Set<String> findByLinkId(Long linkId);

    Set<String> deleteByLinkId(Long linkId);
}
