package backend.academy.scrapper.integration_test.db.repository;

import backend.academy.scrapper.db.repository.ProcessedIdRepository;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.db.repository.impl.ProcessedIdRepositoryImpl;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Import({ProcessedIdRepositoryImpl.class})
@Sql("classpath:test-init.sql")
public class ProcessedIdRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private ProcessedIdRepository repository;

    @Test
    public void findAll_whenLinkContainsProcessedIds_shouldReturnProcessedIdsSet() {
        Set<ProcessedId> processedIds = repository.findAll(1L);

        assertNotNull(processedIds);
        assertFalse(processedIds.isEmpty());
        assertEquals(4, processedIds.size());
    }

    @Test
    public void findAll_whenLinkDoesNotContainProcessedIds_shouldReturnEmptySet() {
        Set<ProcessedId> processedIds = repository.findAll(3L);

        assertNotNull(processedIds);
        assertTrue(processedIds.isEmpty());
    }

    @Test
    public void saveAll_whenLinkNotContainsProcessedIds_shouldSaveAllViaLink() {
        List<ProcessedIdDTO> nowProcessedIds = List.of(
                new ProcessedIdDTO(34L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(37L, ProcessedIdType.STACKOVERFLOW_COMMENT));

        repository.saveAll(4L, nowProcessedIds);

        Set<ProcessedId> processedIds = repository.findAll(4L);
        assertNotNull(processedIds);
        assertFalse(processedIds.isEmpty());
        assertEquals(nowProcessedIds.size(), processedIds.size());
    }

    @Test
    public void saveAll_whenLinkContainsProcessedIds_shouldSaveAllViaLink() {
        List<ProcessedIdDTO> nowProcessedIds = List.of(
                new ProcessedIdDTO(38L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(37L, ProcessedIdType.STACKOVERFLOW_COMMENT));

        repository.saveAll(2L, nowProcessedIds);

        Set<ProcessedId> processedIds = repository.findAll(2L);
        assertNotNull(processedIds);
        assertFalse(processedIds.isEmpty());
        assertEquals(5, processedIds.size());
    }

    @Test
    public void saveAll_whenSaveEqualsProcessedIdEntity_thenDoNotShouldSaveIt() {
        List<ProcessedIdDTO> nowProcessedIds = List.of(new ProcessedIdDTO(34L, ProcessedIdType.GITHUB_PULL_REQUEST));

        repository.saveAll(1L, nowProcessedIds);

        Set<ProcessedId> processedIds = repository.findAll(1L);
        assertNotNull(processedIds);
        assertFalse(processedIds.isEmpty());
        assertEquals(4, processedIds.size());
    }

    @Test
    public void saveAll_whenLinkIdDoesNotExist_shouldThrowException() {
        List<ProcessedIdDTO> nowProcessedIds = List.of(
                new ProcessedIdDTO(34L, ProcessedIdType.STACKOVERFLOW_ANSWER),
                new ProcessedIdDTO(37L, ProcessedIdType.STACKOVERFLOW_COMMENT));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAll(100L, nowProcessedIds));
    }
}
