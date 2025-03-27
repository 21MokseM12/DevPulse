package backend.academy.scrapper.database.jdbc.repository;

import backend.academy.scrapper.build.spring.annotations.SelfAutowired;
import backend.academy.scrapper.database.jdbc.model.Link;
import backend.academy.scrapper.exceptions.LinkNotFoundException;
import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private final JdbcTagRepository tagRepository;

    private final JdbcFilterRepository filterRepository;

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

        tagRepository.save(linkRequest.tags(), linkId);
        filterRepository.save(linkRequest.filters(), linkId);

        return new Link(linkId, linkRequest.link(), linkRequest.tags(), linkRequest.filters(), createdTime);
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
                select id, link, updated_at
                from links
                where id = :link_id
            """;

        Set<Link> links = new HashSet<>();
        jdbcTemplate.query(query, params, rs -> {
            Link entity = new Link();
            entity.id(rs.getLong("id"));
            entity.createdAt(rs.getObject("updated_at", OffsetDateTime.class));
            entity.url(URI.create(rs.getString("link")));
            links.add(entity);
        });

        if (links.size() != 1) {
            return Optional.empty();
        }

        Link resultLink = links.iterator().next();
        resultLink.tags(tagRepository.findByLinkId(linkId));
        resultLink.filters(filterRepository.findByLinkId(linkId));

        return Optional.of(resultLink);
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
        Long linkId = optionalLinkId.orElseThrow(() -> new LinkNotFoundException("Link" + link + " not found"));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);

        Link deletedLink = new Link();
        deletedLink.tags(tagRepository.deleteByLinkId(linkId));
        deletedLink.filters(filterRepository.deleteByLinkId(linkId));

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
                    select id, link, updated_at
                    from links
                    where id in (:link_ids)
            """;

        Set<Link> links = new HashSet<>();
        jdbcTemplate.query(query, sources, rs -> {
            Link entity = new Link();
            entity.id(rs.getLong("id"));
            entity.createdAt(rs.getObject("updated_at", OffsetDateTime.class));
            entity.url(URI.create(rs.getString("link")));
            links.add(entity);
        });

        if (links.isEmpty()) {
            return new ArrayList<>();
        }

        for (Link link : links) {
            link.tags(tagRepository.findByLinkId(link.id()));
            link.filters(filterRepository.findByLinkId(link.id()));
        }

        return links.stream().toList();
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
