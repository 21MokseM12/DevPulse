package backend.academy.scrapper.integration_test.db.repository;

import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.db.repository.ProcessedIdRepository;
import backend.academy.scrapper.db.repository.impl.ProcessedIdRepositoryImpl;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Testcontainers
@ActiveProfiles("test")
@Import(ProcessedIdRepositoryImpl.class)
public class ProcessedIdRepositoryTest extends TestContainersConfiguration {

    @Autowired
    private ProcessedIdRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void findAll_whenLinkHasNoProcessedIds_thenReturnsEmptySet() {
        Long linkId = createLink("https://processed.example/empty");

        Set<ProcessedId> processedIds = repository.findAll(linkId);

        assertTrue(processedIds.isEmpty());
    }

    @Test
    public void saveAll_whenValidPayload_thenAllEntriesArePersisted() {
        Long linkId = createLink("https://processed.example/1");
        List<ProcessedIdDTO> payload = List.of(
            new ProcessedIdDTO(34L, ProcessedIdType.STACKOVERFLOW_ANSWER),
            new ProcessedIdDTO(35L, ProcessedIdType.STACKOVERFLOW_COMMENT),
            new ProcessedIdDTO(36L, ProcessedIdType.GITHUB_ISSUE)
        );

        repository.saveAll(linkId, payload);

        Set<ProcessedId> actual = repository.findAll(linkId);
        assertEquals(
            Set.of(
                new ProcessedId(34L, ProcessedIdType.STACKOVERFLOW_ANSWER.type()),
                new ProcessedId(35L, ProcessedIdType.STACKOVERFLOW_COMMENT.type()),
                new ProcessedId(36L, ProcessedIdType.GITHUB_ISSUE.type())
            ),
            actual
        );
    }

    @Test
    public void saveAll_whenPayloadIsEmpty_thenDoesNothing() {
        Long linkId = createLink("https://processed.example/2");

        repository.saveAll(linkId, List.of());

        assertTrue(repository.findAll(linkId).isEmpty());
    }

    @Test
    public void saveAll_whenPayloadContainsDuplicates_thenStoresEachRow() {
        Long linkId = createLink("https://processed.example/3");
        List<ProcessedIdDTO> payload = List.of(
            new ProcessedIdDTO(77L, ProcessedIdType.GITHUB_PULL_REQUEST),
            new ProcessedIdDTO(77L, ProcessedIdType.GITHUB_PULL_REQUEST)
        );

        repository.saveAll(linkId, payload);

        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from processed_ids where link_id = ? and processed_id = ? and type = ?",
            Integer.class,
            linkId,
            77L,
            ProcessedIdType.GITHUB_PULL_REQUEST.type()
        );
        assertEquals(2, count);
    }

    @Test
    public void saveAll_whenLinkIdDoesNotExist_thenThrowsDataIntegrityViolationException() {
        List<ProcessedIdDTO> payload = List.of(
            new ProcessedIdDTO(34L, ProcessedIdType.STACKOVERFLOW_ANSWER)
        );

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAll(Long.MAX_VALUE, payload));
    }

    @Test
    public void saveAll_whenPayloadIsNull_thenThrowsNullPointerException() {
        Long linkId = createLink("https://processed.example/4");

        assertThrows(NullPointerException.class, () -> repository.saveAll(linkId, null));
    }

    private Long createLink(String url) {
        return jdbcTemplate.queryForObject(
            "insert into links (link, updated_at) values (?, ?) returning id",
            Long.class,
            url,
            Timestamp.from(OffsetDateTime.now().toInstant())
        );
    }
}
