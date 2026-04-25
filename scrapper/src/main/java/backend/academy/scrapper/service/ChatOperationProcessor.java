package backend.academy.scrapper.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatOperationProcessor {
    boolean register(String login, String password);

    boolean unregister(String login, String password);

    boolean existsByLogin(String login);

    Optional<Long> findClientIdByLogin(String login);

    boolean register(Long id);

    boolean unregister(Long id);

    boolean isClient(Long id);

    List<Long> findAllByLinkId(Long linkId);

    void unsubscribe(Long chatId, Long linkId);

    void subscribeChatOnLink(Long chatId, Long linkId);

    void subscribeChatOnLink(Long chatId, Long linkId, Set<String> tags, Set<String> filters);

    boolean chatIsSubscribedOnLink(Long chatId, Long linkId);
}
