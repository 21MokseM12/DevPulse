package backend.academy.scrapper.db;

import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.util.List;
import java.util.Set;

public interface DbCommonService {

    List<Long> findAllLinkIdsByChatId(Long chatId);

    Set<ProcessedId> findAllProcessedIdsByLinkId(Long linkId);

    void saveAllProcessedIdsByLinkId(Long linkId, List<ProcessedIdDTO> processedIds);
}
