package backend.academy.scrapper.database.repository.jdbc;

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
    public boolean subscribeChatOnLink(Long chatId, Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_id", linkId);
        params.addValue("chat_id", chatId);

        RowCountCallbackHandler countCallbackHandler = new RowCountCallbackHandler();
        jdbcTemplate.query(
            "insert into links_chats (chat_id, link_id) values (:chat_id, :link_id)",
            params,
            countCallbackHandler
        );

        return countCallbackHandler.getRowCount() == 1;
    }

    public boolean chatIsSubscribedOnLink(Long chatId, Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String query = """
            select chat_id, link_id
            from links_chats
            where chat_id = :chatId and link_id = :linkId
            """;
        params.addValue("chatId", chatId);
        params.addValue("linkId", linkId);

        RowCountCallbackHandler countCallbackHandler = new RowCountCallbackHandler();
        jdbcTemplate.query(
            query,
            params,
            countCallbackHandler
        );
        return countCallbackHandler.getRowCount() == 1;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void unsubscribed(Long chatId, Long linkId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);
        params.addValue("link_id", linkId);

        jdbcTemplate.query(
            "delete from links_chats where chat_id = :chat_id and link_id = :link_id",
            params,
            new RowCountCallbackHandler()
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void unsubscribed(Long chatId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);

        jdbcTemplate.query(
            "delete from links_chats where chat_id = :chat_id",
            params,
            new RowCountCallbackHandler()
        );
    }

    public List<Long> findAllIdByChatId(Long chatId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);

        return jdbcTemplate.queryForList(
            "select link_id from links_chats where chat_id = :chat_id",
            params,
            Long.class
        );
    }
}
