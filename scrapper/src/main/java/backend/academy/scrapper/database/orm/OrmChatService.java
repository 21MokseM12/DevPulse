package backend.academy.scrapper.database.orm;

import backend.academy.scrapper.database.ChatService;
import backend.academy.scrapper.database.orm.entity.ChatEntity;
import backend.academy.scrapper.database.orm.repository.OrmChatRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrmChatService implements ChatService {

    private static final Logger LOG = LogManager.getLogger(OrmChatService.class);

    private final OrmChatRepository ormChatRepository;

    @Autowired
    public OrmChatService(OrmChatRepository ormChatRepository) {
        this.ormChatRepository = ormChatRepository;
    }

    @Transactional
    @Override
    public boolean register(Long id) {
        if (!ormChatRepository.existsById(id)) {
            ormChatRepository.save(new ChatEntity(id));
            LOG.info("Client registered with id {}", id);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public boolean unregister(Long id) {
        if (ormChatRepository.existsById(id)) {
            ormChatRepository.deleteById(id);
            LOG.info("Client unregistered with id {}", id);
            return true;
        }
        return false;
    }
}
