package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.db.repository.ChatRepository;
import backend.academy.scrapper.db.repository.LinkToChatRepository;
import backend.academy.scrapper.service.ChatOperationProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOperationProcessorImpl implements ChatOperationProcessor {

    private final ChatRepository chatRepository;
    private final LinkToChatRepository linkToChatRepository;

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
    public void unsubscribe(Long chatId, Long linkId) {
        linkToChatRepository.unsubscribe(chatId, linkId);
    }

    @Override
    public void subscribeChatOnLink(Long chatId, Long linkId) {
        linkToChatRepository.subscribeChatOnLink(chatId, linkId);
    }

    @Override
    public boolean chatIsSubscribedOnLink(Long chatId, Long linkId) {
        return linkToChatRepository.subscribeChatOnLink(chatId, linkId);
    }
}
