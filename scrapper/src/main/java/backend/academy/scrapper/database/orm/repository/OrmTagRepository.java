package backend.academy.scrapper.database.orm.repository;

import backend.academy.scrapper.database.orm.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrmTagRepository extends JpaRepository<TagEntity, Long> {}
