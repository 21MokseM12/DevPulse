package backend.academy.scrapper.database.jdbc.repository;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcLinkToChatRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean subscribeChatOnLink(@NotNull Long chatId, @NotNull Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);
        params.addValue("chat_id", chatId);

        RowCountCallbackHandler countCallbackHandler = new RowCountCallbackHandler();
        jdbcTemplate.query(
                "insert into links_chats (chat_id, link_id) values (:chat_id, :link_id) returning 1", params, countCallbackHandler);

        return countCallbackHandler.getRowCount() == 1;
    }

    @Transactional(readOnly = true)
    public boolean chatIsSubscribedOnLink(Long chatId, Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String query =
                """
            select chat_id, link_id
            from links_chats
            where chat_id = :chatId and link_id = :linkId
            """;
        params.addValue("chatId", chatId);
        params.addValue("linkId", linkId);

        RowCountCallbackHandler countCallbackHandler = new RowCountCallbackHandler();
        jdbcTemplate.query(query, params, countCallbackHandler);
        return countCallbackHandler.getRowCount() == 1;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void unsubscribe(Long chatId, Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);
        params.addValue("link_id", linkId);

        jdbcTemplate.query(
                "delete from links_chats where chat_id = :chat_id and link_id = :link_id returning chat_id",
                params,
                new RowCountCallbackHandler());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void unsubscribeAll(Long chatId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);

        jdbcTemplate.query("delete from links_chats where chat_id = :chat_id returning 1", params, new RowCountCallbackHandler());
    }

    @Transactional(readOnly = true)
    public List<Long> findAllIdByChatId(Long chatId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);

        return jdbcTemplate.queryForList(
                "select link_id from links_chats where chat_id = :chat_id", params, Long.class);
    }

    @Transactional(readOnly = true)
    public List<Long> findAllByLinkId(Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);

        return jdbcTemplate.queryForList(
            "select chat_id from links_chats where link_id = :linkId",
            params,
            Long.class
        );
    }
}
