package backend.academy.scrapper.integration_test.db.repository;

import backend.academy.scrapper.db.repository.ChatRepository;
import backend.academy.scrapper.db.repository.impl.ChatRepositoryImpl;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Import(ChatRepositoryImpl.class)
public class ChatRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private ChatRepository repository;

    @Test
    public void save_whenClientDoesNotExist_thenClientIsStored() {
        Long id = 10101L;

        repository.save(id);

        assertTrue(repository.isClient(id));
    }

    @Test
    public void save_whenClientAlreadyExists_thenThrowsDuplicateKeyException() {
        Long id = 10102L;
        repository.save(id);

        assertThrows(DuplicateKeyException.class, () -> repository.save(id));
    }

    @Test
    public void delete_whenClientExists_thenReturnsTrueAndRemovesClient() {
        Long id = 10103L;
        repository.save(id);

        boolean deleted = repository.delete(id);

        assertTrue(deleted);
        assertFalse(repository.isClient(id));
    }

    @Test
    public void delete_whenClientDoesNotExist_thenReturnsFalse() {
        Long id = 10104L;

        boolean deleted = repository.delete(id);

        assertFalse(deleted);
    }

    @Test
    public void isClient_whenIdIsNull_thenReturnsFalse() {
        assertFalse(repository.isClient(null));
    }
}
