package backend.academy.scrapper.db.impl;

import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.repository.FilterRepository;
import backend.academy.scrapper.db.repository.LinkRepository;
import backend.academy.scrapper.db.repository.TagRepository;
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
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;
    private final LinkMapper mapper;

    @Override
    @Transactional(rollbackFor = DataAccessException.class)
    public Link saveLink(AddLinkRequest request) {
        log.info("Начинается сохранение ссылки по запросу: {}", request);
        OffsetDateTime createdTime = OffsetDateTime.now(clock);
        Long linkId = linkRepository.save(request.link().toString(), createdTime);
        tagRepository.save(request.tags(), linkId);
        filterRepository.save(request.filters(), linkId);
        log.info("Ссылка успешно сохранена с id: {}", linkId);
        return mapper.toLink(request, linkId, createdTime);
    }

    @Override
    public Optional<Long> findIdByLink(String link) {
        return linkRepository.findIdByLink(link);
    }

    @Override
    public Optional<Link> findById(Long id) {
        return linkRepository.findById(id);
    }

    @Override
    public boolean existsLink(String link) {
        return linkRepository.existsLink(link);
    }

    @Override
    @Transactional(rollbackFor = DataAccessException.class)
    public Optional<Link> delete(String link) {
        log.info("Начинается удаление ссылки: {}", link);
        Optional<Long> optId = linkRepository.findIdByLink(link);
        if (optId.isPresent()) {
            Long id = optId.get();
            Set<String> tags = tagRepository.deleteByLinkId(id);
            Set<String> filters = filterRepository.deleteByLinkId(id);
            return linkRepository.delete(id)
                .map(linkEntity -> mapper.toLink(linkEntity, tags, filters));
        }
        log.warn("id для ссылки {} не найден", link);
        return Optional.empty();
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
        int offset = offsetMultiplier * limit;
        return linkRepository.findAllLinksByUpdatedAt(highestTimeLimit, offset, limit);
    }
}
