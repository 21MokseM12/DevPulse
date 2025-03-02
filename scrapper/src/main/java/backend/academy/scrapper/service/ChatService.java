package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.ClientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger LOG = LogManager.getLogger(ChatService.class);

    private final ClientRepository clientRepository;

    @Autowired
    public ChatService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public boolean register(Long id) {
        if (!clientRepository.isClient(id)) {
            clientRepository.register(id);
            LOG.info("Client registered with id {}", id);
            return true;
        } else {
            return false;
        }
    }

    public boolean unregister(Long id) {
        if (clientRepository.isClient(id)) {
            clientRepository.unregister(id);
            LOG.info("Client unregistered with id {}", id);
            return true;
        } else {
            LOG.info("Client unregistered with id {} failure", id);
            return false;
        }
    }
}
