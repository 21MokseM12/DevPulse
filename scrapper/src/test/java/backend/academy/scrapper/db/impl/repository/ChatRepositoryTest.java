package backend.academy.scrapper.db.impl.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.db.TestContainersConfiguration;
import backend.academy.scrapper.db.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
@Import(ChatRepository.class)
@ActiveProfiles("test")
@Sql("classpath:test-init.sql")
public class ChatRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private ChatRepository repo;

    @Test
    public void whenClientExists_thenReturnTrue() {
        Long id = 123L;
        boolean exist = repo.isClient(id);
        assertTrue(exist);
    }

    @Test
    public void whenClientDoesNotExist_thenReturnFalse() {
        Long id = 456L;
        boolean exist = repo.isClient(id);
        assertFalse(exist);
    }

    @Test
    public void testSaveClientSuccess() {
        Long id = 9L;
        repo.save(id);
        assertTrue(repo.isClient(id));
    }

    @Test
    public void testSaveClientFailure() {
        Long id = 1L;
        DuplicateKeyException exception = assertThrows(DuplicateKeyException.class, () -> repo.save(id));
    }

    @Test
    public void testDeleteClientSuccess() {
        Long id = 1L;
        boolean deleted = repo.delete(id);
        assertTrue(deleted);
    }

    @Test
    public void testDeleteClientFailure() {
        Long id = 12345L;
        boolean deleted = repo.delete(id);
        assertFalse(deleted);
    }
}
