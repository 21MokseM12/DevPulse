package backend.academy.scrapper.database.orm.repository;

import backend.academy.scrapper.database.orm.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrmLinkRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByLink(String link);
}
