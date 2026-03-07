package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.DbCommonService;
import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.db.repository.ProcessedIdRepository;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbCommonServiceImpl implements DbCommonService {

    private final LinkToChatRepository linkToChatRepository;
    private final ProcessedIdRepository processedIdRepository;

    @Override
    public List<Long> findAllLinkIdsByChatId(Long chatId) {
        return linkToChatRepository.findAllIdByChatId(chatId);
    }

    @Override
    public Set<ProcessedId> findAllProcessedIdsByLinkId(Long linkId) {
        return processedIdRepository.findAll(linkId);
    }

    @Override
    public void saveAllProcessedIdsByLinkId(Long linkId, List<ProcessedIdDTO> processedIds) {
        processedIdRepository.saveAll(linkId, processedIds);
    }
}
