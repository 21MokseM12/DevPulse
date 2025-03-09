package backend.academy.scrapper.database.jdbc;

import backend.academy.scrapper.database.ChatService;
import backend.academy.scrapper.database.repository.jdbc.JdbcChatRepository;
import backend.academy.scrapper.database.repository.jdbc.JdbcLinkToChatRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JdbcChatService implements ChatService {

    private static final Logger LOG = LogManager.getLogger(JdbcChatService.class);

    private final JdbcChatRepository chatRepository;

    private final JdbcLinkToChatRepository linkToChatRepository;

    @Override
    @Transactional
    public boolean register(Long id) {
        if (!chatRepository.isClient(id)) {
            chatRepository.save(id);
            LOG.info("Client registered with id {}", id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean unregister(Long id) {
        if (chatRepository.isClient(id)) {
            linkToChatRepository.unsubscribed(id);
            chatRepository.delete(id);
            LOG.info("Client unregistered with id {}", id);
            return true;
        } else {
            LOG.info("Client unregistered with id {} failure", id);
            return false;
        }
    }
}
