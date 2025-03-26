package backend.academy.scrapper.database.orm.repository;

import backend.academy.scrapper.database.orm.entity.FilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrmFilterRepository extends JpaRepository<FilterEntity, Long> {
}
