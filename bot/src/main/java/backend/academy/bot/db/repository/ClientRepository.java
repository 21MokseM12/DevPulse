package backend.academy.bot.db.repository;

import backend.academy.bot.db.model.Client;
import java.util.Optional;

public interface ClientRepository {
    Optional<Client> findByLogin(String login);

    long save(String login, String password);

    boolean deleteByLogin(String login);
}
