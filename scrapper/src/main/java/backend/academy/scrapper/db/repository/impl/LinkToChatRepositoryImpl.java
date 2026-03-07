package backend.academy.scrapper.db.repository.impl;

import backend.academy.scrapper.db.query.LinkToChatQuery;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class LinkToChatRepositoryImpl implements LinkToChatRepository {

    private static final String LINK_ID = "linkId";
    private static final String CHAT_ID = "chatId";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean subscribeChatOnLink(@NotNull Long chatId, @NotNull Long linkId) {
        return jdbcTemplate.update(
            LinkToChatQuery.INSERT.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId)
                .addValue(CHAT_ID, chatId)
        ) == 1;
    }

    @Transactional(readOnly = true)
    public boolean chatIsSubscribedOnLink(Long chatId, Long linkId) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
            LinkToChatQuery.SELECT_COUNT_BY_CHAT_ID_AND_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(CHAT_ID, chatId)
                .addValue(LINK_ID, linkId),
            Long.class
        )).orElse(0L) == 1;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean unsubscribe(Long chatId, Long linkId) {
        return jdbcTemplate.update(
            LinkToChatQuery.DELETE_BY_CHAT_ID_AND_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(CHAT_ID, chatId)
                .addValue(LINK_ID, linkId)
        ) > 0;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void unsubscribeAll(Long chatId) {
        jdbcTemplate.update(
            LinkToChatQuery.DELETE_BY_CHAT_ID.query(),
            new MapSqlParameterSource()
                .addValue(CHAT_ID, chatId)
        );
    }

    @Transactional(readOnly = true)
    public List<Long> findAllIdByChatId(Long chatId) {
        return jdbcTemplate.queryForList(
            LinkToChatQuery.SELECT_LINKS_BY_CHAT_ID.query(),
            new MapSqlParameterSource()
                .addValue(CHAT_ID, chatId),
            Long.class
        );
    }

    @Transactional(readOnly = true)
    public List<Long> findAllByLinkId(Long linkId) {
        return jdbcTemplate.queryForList(
            LinkToChatQuery.SELECT_CHAT_BY_LINK_ID.query(),
            new MapSqlParameterSource()
                .addValue(LINK_ID, linkId),
            Long.class
        );
    }
}
