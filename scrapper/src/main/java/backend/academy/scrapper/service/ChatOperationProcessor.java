package backend.academy.scrapper.service;

import java.util.List;

public interface ChatOperationProcessor {
    boolean register(Long id);

    boolean unregister(Long id);

    boolean isClient(Long id);

    List<Long> findAllByLinkId(Long linkId);

    void unsubscribe(Long chatId, Long linkId);

    void subscribeChatOnLink(Long chatId, Long linkId);

    boolean chatIsSubscribedOnLink(Long chatId, Long linkId);
}
