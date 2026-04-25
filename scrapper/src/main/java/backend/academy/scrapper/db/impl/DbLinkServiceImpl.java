package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.repository.LinkRepository;
import backend.academy.scrapper.mapper.LinkMapper;
import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbLinkServiceImpl implements DbLinkService {

    private final Clock clock;
    private final LinkMapper mapper;
    private final LinkRepository linkRepository;

    @Override
    @Transactional(rollbackFor = DataAccessException.class)
    public Link saveLink(AddLinkRequest request) {
        try {
            log.info("Начинается сохранение ссылки по запросу: {}", request);
            OffsetDateTime createdTime = OffsetDateTime.now(clock);
            Long linkId = linkRepository.save(request.link().toString(), createdTime);
            log.info("Ссылка успешно сохранена с id: {}", linkId);
            return mapper.toLink(request, linkId, createdTime);
        } catch (DataAccessException e) {
            log.warn("Ошибка при сохранении ссылки по запросу: {}", request);
            throw e;
        }
    }

    @Override
    public Optional<Link> findByLink(String link) {
        try {
            return linkRepository.findIdByLink(link);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при поиске ссылки {}: {}", link, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Link> findById(Long id) {
        try {
            return linkRepository.findById(id);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при поиске ссылки по id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean existsLink(String link) {
        try {
            return linkRepository.existsLink(link);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при проверке существования ссылки: {}", link);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = DataAccessException.class)
    public Optional<Link> delete(String link) {
        try {
            log.info("Начинается удаление ссылки: {}", link);
            Optional<Link> optId = linkRepository.findIdByLink(link);
            if (optId.isPresent()) {
                Long id = optId.get().id();
                return linkRepository.delete(id);
            }
            log.warn("id для ссылки {} не найден", link);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при удалении ссылки {}: {}", link, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Link> findAllLinks(List<Long> linkIds) {
        log.info("Начинается поиск ссылок по списку id: {}", linkIds);
        if (!linkIds.isEmpty()) {
            return linkIds.stream()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
        }
        log.warn("Переданный список id ссылок пуст");
        return List.of();
    }

    @Override
    public Set<URI> findAllLinksByUpdatedAt(OffsetDateTime highestTimeLimit, int offsetMultiplier, Integer limit) {
        try {
            int offset = offsetMultiplier * limit;
            return linkRepository.findAllLinksByUpdatedAt(highestTimeLimit, offset, limit);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при поиске ссылок по дате обновления: {}", e.getMessage());
            return Set.of();
        }
    }
}
