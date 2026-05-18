package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.model.ClientAuthData;
import java.util.List;
import java.util.Optional;

public interface ChatRepository {
    boolean existsByLogin(String login);

    Optional<ClientAuthData> findAuthDataByLogin(String login);

    Optional<Long> findIdByLogin(String login);

    List<String> findLoginsByIds(List<Long> ids);

    void save(String login, String passwordHash);

    boolean deleteByLogin(String login);

    boolean isClient(Long id);

    void save(Long id);

    boolean delete(Long id);
}
