package backend.academy.scrapper.database.orm.repository;

import backend.academy.scrapper.database.orm.entity.LinkEntity;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrmLinkRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByLink(String link);

    Page<LinkEntity> findLinkEntitiesByUpdatedAtBefore(OffsetDateTime updatedAtAfter, Pageable pageable);
}
