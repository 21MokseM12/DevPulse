package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

    private final ClientRepository clientRepository;

    @Autowired
    public ChatService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public boolean register(Long id) {
        if (!clientRepository.isClient(id)) {
            clientRepository.register(id);
            return true;
        } else {
            log.info("Client registered with id {}", id);
            return false;
        }
    }

    public boolean unregister(Long id) {
        if (clientRepository.isClient(id)) {
            clientRepository.unregister(id);
            log.info("Client unregistered with id {}", id);
            return true;
        } else {
            log.info("Client unregistered with id {} failure", id);
            return false;
        }
    }
}
