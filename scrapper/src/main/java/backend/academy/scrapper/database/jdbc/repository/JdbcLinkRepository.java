package backend.academy.scrapper.database.jdbc.repository;

import backend.academy.scrapper.build.spring.annotations.SelfAutowired;
import backend.academy.scrapper.database.jdbc.model.Link;
import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;

@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository {

    private final Clock clock;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SelfAutowired
    private JdbcLinkRepository jdbcLinkRepository;

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = DataAccessException.class)
    public Link save(AddLinkRequest linkRequest) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        OffsetDateTime createdTime = OffsetDateTime.now(clock);

        params.addValue("link", linkRequest.link().toString());
        params.addValue("updated_at", createdTime);
        Long linkId = jdbcTemplate.queryForObject(
                "insert into links (link, updated_at) values (:link, :updated_at) returning id", params, Long.class);

        jdbcLinkRepository.saveLinkTags(linkRequest.tags(), linkId);
        jdbcLinkRepository.saveLinkFilters(linkRequest.filters(), linkId);

        return new Link(linkId, linkRequest.link(), linkRequest.tags(), linkRequest.filters(), createdTime);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveLinkTags(Set<String> tags, Long linkId) {
        List<MapSqlParameterSource> tagSources = new ArrayList<>();
        for (String tag : tags) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
            mapSqlParameterSource.addValue("tag", tag);
            mapSqlParameterSource.addValue("link_id", linkId);
            tagSources.add(mapSqlParameterSource);
        }
        jdbcTemplate.batchUpdate(
                "insert into tags (tag, link_id) values (:tag, :link_id)",
                tagSources.toArray(new MapSqlParameterSource[] {}));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveLinkFilters(Set<String> filters, Long linkId) {
        List<MapSqlParameterSource> filterSources = new ArrayList<>();
        for (String filter : filters) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
            mapSqlParameterSource.addValue("filter", filter);
            mapSqlParameterSource.addValue("link_id", linkId);
            filterSources.add(mapSqlParameterSource);
        }
        jdbcTemplate.batchUpdate(
                "insert into filters (filter, link_id) values (:filter, :link_id)",
                filterSources.toArray(new MapSqlParameterSource[] {}));
    }

    @Transactional
    public Optional<Long> findByLink(String link) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link", link);

        return jdbcTemplate
                .query("select id from links where link = :link", params, (rs, rowNum) -> rs.getLong("id"))
                .stream()
                .findFirst();
    }

    @Transactional
    public Optional<Link> findById(Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);
        String query =
                """
            select l.id, l.link, l.updated_at, t.tag, f.filter
            from links l
            left join tags t on t.link_id = l.id
            left join filters f on f.link_id = l.id
            where l.id = :link_id
            """;

        Map<Long, Link> linkMap = new HashMap<>();
        jdbcTemplate.query(query, params, rs -> {
            Long id = rs.getLong("id");
            Link link = linkMap.get(id);
            if (link == null) {
                link = new Link(
                        id,
                        URI.create(rs.getString("link")),
                        new HashSet<>(),
                        new HashSet<>(),
                        rs.getObject("updated_at", OffsetDateTime.class));
                linkMap.put(linkId, link);
            }
            String tag = rs.getString("tag");
            if (tag != null) {
                link.tags().add(tag);
            }

            String filter = rs.getString("filter");
            if (filter != null) {
                link.filters().add(filter);
            }
        });

        if (linkMap.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(linkMap.get(linkId));
    }

    @Transactional
    public boolean existsLink(String link) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link", link);

        Integer linkCount =
                jdbcTemplate.queryForObject("select count(link) from links where link = :link", params, Integer.class);

        return Optional.ofNullable(linkCount).orElse(0) == 1;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Link> delete(String link) {
        Optional<Long> optionalLinkId = jdbcLinkRepository.findByLink(link);

        if (optionalLinkId.isEmpty()) {
            return Optional.empty();
        }
        Long linkId = optionalLinkId.get();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);

        Link deletedLink = new Link();
        deletedLink.tags(new HashSet<>());
        deletedLink.filters(new HashSet<>());

        jdbcTemplate.query("delete from tags where link_id = :link_id returning tag", params, rs -> {
            deletedLink.tags().add(rs.getString("tag"));
        });

        jdbcTemplate.query("delete from filters where link_id = :link_id returning filter", params, rs -> {
            deletedLink.filters().add(rs.getString("filter"));
        });

        jdbcTemplate.query("delete from links where id = :link_id returning id, link, updated_at", params, rs -> {
            deletedLink.id(rs.getLong("id"));
            deletedLink.url(URI.create(rs.getString("link")));
            deletedLink.createdAt(rs.getObject("updated_at", OffsetDateTime.class));
        });

        return Optional.of(deletedLink);
    }

    @Transactional
    public List<Link> findAllLinks(List<Long> linkIds) {
        if (linkIds.isEmpty()) {
            return new ArrayList<>();
        }
        MapSqlParameterSource sources = new MapSqlParameterSource();
        sources.addValue("link_ids", linkIds);

        String query =
                """
            select l.id, l.link, l.updated_at, t.tag, f.filter
            from links l
            left join tags t on t.link_id = l.id
            left join filters f on f.link_id = l.id
            where l.id in (:link_ids)
        """;

        Map<Long, Link> linkMap = new HashMap<>();
        jdbcTemplate.query(query, sources, rs -> {
            Long id = rs.getLong("id");
            Link link = linkMap.get(id);
            if (link == null) {
                link = new Link(
                        id,
                        URI.create(rs.getString("link")),
                        new HashSet<>(),
                        new HashSet<>(),
                        rs.getObject("updated_at", OffsetDateTime.class));
                linkMap.put(id, link);
            }
            String tag = rs.getString("tag");
            if (tag != null) {
                link.tags().add(tag);
            }

            String filter = rs.getString("filter");
            if (filter != null) {
                link.filters().add(filter);
            }
        });

        if (linkMap.isEmpty()) {
            return new ArrayList<>();
        }
        return linkMap.values().stream().toList();
    }

    @Transactional(readOnly = true)
    public Set<URI> findAllLinksByUpdatedAt(OffsetDateTime highestTimeLimit, int offsetMultiplier, Integer limit) {
        int offset = offsetMultiplier * limit;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("highestTimeLimit", highestTimeLimit.toLocalDateTime());
        params.addValue("offset", offset);
        params.addValue("limit", limit);

        return jdbcTemplate
                .queryForList(
                        "select link from links where updated_at <= :highestTimeLimit limit :limit offset :offset",
                        params,
                        String.class)
                .stream()
                .map(URI::create)
                .collect(Collectors.toSet());
    }
}
