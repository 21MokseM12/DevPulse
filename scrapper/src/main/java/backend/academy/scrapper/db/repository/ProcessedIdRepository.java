package backend.academy.scrapper.db.repository;

import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.util.List;
import java.util.Set;

public interface ProcessedIdRepository {

    Set<ProcessedId> findAll(Long linkId);

    void saveAll(Long linkId, List<ProcessedIdDTO> nowProcessedIds);
}
