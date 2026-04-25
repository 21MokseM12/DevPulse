package backend.academy.scrapper.db.repository;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public interface LinkToChatRepository {

    boolean subscribeChatOnLink(@NotNull Long chatId, @NotNull Long linkId, Set<String> tags, Set<String> filters);

    default boolean subscribeChatOnLink(@NotNull Long chatId, @NotNull Long linkId) {
        return subscribeChatOnLink(chatId, linkId, Set.of(), Set.of());
    }

    /** Read-only: true if a row exists for the pair (chat_id, link_id). Does not insert. */
    boolean chatIsSubscribedOnLink(@NotNull Long chatId, @NotNull Long linkId);

    boolean unsubscribe(Long chatId, Long linkId);

    void unsubscribeAll(Long chatId);

    List<Long> findAllIdByChatId(Long chatId);

    List<Long> findAllByLinkId(Long linkId);
}
