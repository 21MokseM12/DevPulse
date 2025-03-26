package backend.academy.scrapper.database.orm.repository;

import backend.academy.scrapper.database.orm.entity.ProcessedIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrmProcessedIdsRepository extends JpaRepository<ProcessedIdEntity, Long> {
}
