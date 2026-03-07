package backend.academy.scrapper.db.repository;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface LinkToChatRepository {

    boolean subscribeChatOnLink(@NotNull Long chatId, @NotNull Long linkId);

    boolean chatIsSubscribedOnLink(Long chatId, Long linkId);

    boolean unsubscribe(Long chatId, Long linkId);

    void unsubscribeAll(Long chatId);

    List<Long> findAllIdByChatId(Long chatId);

    List<Long> findAllByLinkId(Long linkId);
}
