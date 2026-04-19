package backend.academy.scrapper.db.repository;

import java.util.Optional;

public interface ChatRepository {
    boolean existsByLogin(String login);

    boolean isClient(String login, String password);

    Optional<Long> findIdByCredentials(String login, String password);

    Optional<Long> findIdByLogin(String login);

    void save(String login, String password);

    boolean delete(String login, String password);

    boolean isClient(Long id);

    void save(Long id);

    boolean delete(Long id);
}
