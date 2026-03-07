package backend.academy.scrapper.db.repository;

public interface ChatRepository {

    boolean isClient(Long id);

    void save(Long id);

    boolean delete(Long id);
}
