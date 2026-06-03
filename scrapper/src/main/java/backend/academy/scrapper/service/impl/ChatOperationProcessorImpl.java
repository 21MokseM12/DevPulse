package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.db.model.Link;
import backend.academy.scrapper.db.model.LinkSubscription;
import backend.academy.scrapper.db.repository.ChatRepository;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.exceptions.InvalidCredentialsException;
import backend.academy.scrapper.service.ChatOperationProcessor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOperationProcessorImpl implements ChatOperationProcessor {

    private final ChatRepository chatRepository;
    private final LinkToChatRepository linkToChatRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public boolean register(@NonNull String login, @NonNull String password) {
        if (chatRepository.existsByLogin(login)) {
            log.info("Произошла ошибка при регистрации клиента с login {}", login);
            return false;
        }
        chatRepository.save(login, passwordEncoder.encode(password));
        log.info("Клиент с login {} успешно зарегистрирован", login);
        return true;
    }

    @Override
    @Transactional
    public boolean unregister(@NonNull String login, @NonNull String password) {
        var authData = chatRepository.findAuthDataByLogin(login);
        if (authData.isEmpty()) {
            log.info("Произошла ошибка при удалении клиента с login {}", login);
            return false;
        }
        var chatAuthData = authData.orElseThrow();
        String passwordHash = chatAuthData.passwordHash();
        if (passwordHash == null || passwordHash.isBlank() || !passwordEncoder.matches(password, passwordHash)) {
            throw new InvalidCredentialsException("Некорректные учетные данные");
        }
        linkToChatRepository.unsubscribeAll(chatAuthData.id());
        chatRepository.deleteByLogin(login);
        log.info("Клиент с login {} успешно удален", login);
        return true;
    }

    @Override
    public boolean existsByLogin(String login) {
        return chatRepository.existsByLogin(login);
    }

    @Override
    public Optional<Long> findClientIdByLogin(String login) {
        return chatRepository.findIdByLogin(login);
    }

    @Override
    public List<String> findLoginsByIds(List<Long> ids) {
        return chatRepository.findLoginsByIds(ids);
    }

    @Override
    @Transactional
    public boolean register(@NonNull Long id) {
        if (!chatRepository.isClient(id)) {
            chatRepository.save(id);
            log.info("Клиент с id {} успешно зарегистрирован", id);
            return true;
        } else {
            log.info("Произошла ошибка при регистрации клиента с id {}", id);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean unregister(@NonNull Long id) {
        if (chatRepository.isClient(id)) {
            linkToChatRepository.unsubscribeAll(id);
            chatRepository.delete(id);
            log.info("Клиент с id {} успешно удален", id);
            return true;
        } else {
            log.info("Произошла ошибка при удалении клиента с id {}", id);
            return false;
        }
    }

    @Override
    public boolean isClient(Long id) {
        try {
            return chatRepository.isClient(id);
        } catch (DataAccessException e) {
            log.warn("Произошла ошибка при попытке поиска клиента с id {}", id);
            return false;
        }
    }

    @Override
    public List<Long> findAllByLinkId(Long linkId) {
        return linkToChatRepository.findAllByLinkId(linkId);
    }

    @Override
    public List<Link> findAllLinksWithMetadataByChatId(Long chatId) {
        return linkToChatRepository.findAllLinksWithMetadataByChatId(chatId);
    }

    @Override
    public List<LinkSubscription> findSubscriptionsByLinkId(Long linkId) {
        return linkToChatRepository.findSubscriptionsByLinkId(linkId);
    }

    @Override
    public void unsubscribe(Long chatId, Long linkId) {
        linkToChatRepository.unsubscribe(chatId, linkId);
    }

    @Override
    public void subscribeChatOnLink(Long chatId, Long linkId) {
        linkToChatRepository.subscribeChatOnLink(chatId, linkId);
    }

    @Override
    public void subscribeChatOnLink(Long chatId, Long linkId, Set<String> tags, Set<String> filters) {
        linkToChatRepository.subscribeChatOnLink(chatId, linkId, tags, filters);
    }

    @Override
    public boolean chatIsSubscribedOnLink(Long chatId, Long linkId) {
        return linkToChatRepository.chatIsSubscribedOnLink(chatId, linkId);
    }
}
