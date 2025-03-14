package backend.academy.scrapper.database.orm.repository;

import backend.academy.scrapper.database.orm.entity.ChatEntity;
import backend.academy.scrapper.database.orm.entity.LinkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmChatRepository extends JpaRepository<ChatEntity, Long> {
    List<LinkEntity> findAllById(Long id);
}
