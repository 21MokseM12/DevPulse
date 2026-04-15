package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.DbCommonService;
import backend.academy.scrapper.db.model.ProcessedId;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.db.repository.ProcessedIdRepository;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbCommonServiceImpl implements DbCommonService {

    private final LinkToChatRepository linkToChatRepository;
    private final ProcessedIdRepository processedIdRepository;

    @Override
    public List<Long> findAllLinkIdsByChatId(Long chatId) {
        try {
            return linkToChatRepository.findAllIdByChatId(chatId);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при поиске ссылок по id чата: {}", chatId);
            return List.of();
        }
    }

    @Override
    public Set<ProcessedId> findAllProcessedIdsByLinkId(Long linkId) {
        try {
            return processedIdRepository.findAll(linkId);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при поиске всех обработанных id по id ссылки: {}", linkId);
            return Set.of();
        }
    }

    @Override
    public void saveAllProcessedIdsByLinkId(Long linkId, List<ProcessedIdDTO> processedIds) {
        try {
            processedIdRepository.saveAll(linkId, processedIds);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при сохранении обработанных id по ссылку с id: {}", linkId);
            //todo подумать над тем, что вернуть
        }
    }
}
