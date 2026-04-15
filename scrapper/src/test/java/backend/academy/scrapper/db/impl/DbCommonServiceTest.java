package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.db.repository.ProcessedIdRepository;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DbCommonServiceTest {

    @Mock
    private LinkToChatRepository linkToChatRepository;
    @Mock
    private ProcessedIdRepository processedIdRepository;
    @InjectMocks
    private DbCommonServiceImpl dbCommonService;

    @Test
    public void findAllLinkIdsByChatId_success() {
        Long chatId = 1L;
        List<Long> expectedLinkIds = List.of(10L, 11L);
        when(linkToChatRepository.findAllIdByChatId(chatId)).thenReturn(expectedLinkIds);

        List<Long> result = dbCommonService.findAllLinkIdsByChatId(chatId);

        assertEquals(expectedLinkIds, result);
        verify(linkToChatRepository, times(1)).findAllIdByChatId(chatId);
    }

    @Test
    public void findAllLinkIdsByChatId_whenRepositoryThrows_shouldReturnEmptyList() {
        Long chatId = 1L;
        when(linkToChatRepository.findAllIdByChatId(chatId))
            .thenThrow(dataAccessException("cannot read links by chat id"));

        List<Long> result = dbCommonService.findAllLinkIdsByChatId(chatId);

        assertTrue(result.isEmpty());
        verify(linkToChatRepository, times(1)).findAllIdByChatId(chatId);
    }

    @Test
    public void findAllProcessedIdsByLinkId_success() {
        Long linkId = 7L;
        Set<ProcessedId> expectedProcessedIds = Set.of(
            new ProcessedId(100L, ProcessedIdType.GITHUB_ISSUE.type()),
            new ProcessedId(101L, ProcessedIdType.GITHUB_PULL_REQUEST.type())
        );
        when(processedIdRepository.findAll(linkId)).thenReturn(expectedProcessedIds);

        Set<ProcessedId> result = dbCommonService.findAllProcessedIdsByLinkId(linkId);

        assertEquals(expectedProcessedIds, result);
        verify(processedIdRepository, times(1)).findAll(linkId);
    }

    @Test
    public void findAllProcessedIdsByLinkId_whenRepositoryThrows_shouldReturnEmptySet() {
        Long linkId = 7L;
        when(processedIdRepository.findAll(linkId))
            .thenThrow(dataAccessException("cannot read processed ids by link id"));

        Set<ProcessedId> result = dbCommonService.findAllProcessedIdsByLinkId(linkId);

        assertTrue(result.isEmpty());
        verify(processedIdRepository, times(1)).findAll(linkId);
    }

    @Test
    public void saveAllProcessedIdsByLinkId_success() {
        Long linkId = 7L;
        List<ProcessedIdDTO> processedIds = List.of(
            new ProcessedIdDTO(100L, ProcessedIdType.GITHUB_ISSUE),
            new ProcessedIdDTO(101L, ProcessedIdType.STACKOVERFLOW_ANSWER)
        );

        dbCommonService.saveAllProcessedIdsByLinkId(linkId, processedIds);

        verify(processedIdRepository, times(1)).saveAll(linkId, processedIds);
    }

    @Test
    public void saveAllProcessedIdsByLinkId_whenRepositoryThrows_shouldNotThrow() {
        Long linkId = 7L;
        List<ProcessedIdDTO> processedIds = List.of(
            new ProcessedIdDTO(100L, ProcessedIdType.GITHUB_ISSUE),
            new ProcessedIdDTO(101L, ProcessedIdType.STACKOVERFLOW_ANSWER)
        );
        doThrow(dataAccessException("cannot save processed ids"))
            .when(processedIdRepository).saveAll(linkId, processedIds);

        assertDoesNotThrow(() -> dbCommonService.saveAllProcessedIdsByLinkId(linkId, processedIds));
        verify(processedIdRepository, times(1)).saveAll(linkId, processedIds);
    }

    private DataAccessException dataAccessException(String message) {
        return new RecoverableDataAccessException(message);
    }
}
