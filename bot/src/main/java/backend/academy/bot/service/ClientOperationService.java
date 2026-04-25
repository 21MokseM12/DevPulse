package backend.academy.bot.service;

import backend.academy.bot.db.repository.ClientRepository;
import backend.academy.bot.exceptions.ChatNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Service
@RequiredArgsConstructor
public class ClientOperationService {

    private final ClientRepository clientRepository;

    public void registerClient(String login, String password) throws BadRequestException {
        validateCredentials(login, password);
        if (clientRepository.findByLogin(login).isPresent()) {
            throw new BadRequestException("Клиент уже зарегистрирован");
        }
        clientRepository.save(login, password);
    }

    public void unregisterClient(String login, String password) throws BadRequestException {
        validateCredentials(login, password);
        var storedClient = clientRepository.findByLogin(login)
                .orElseThrow(() -> new ChatNotFoundException("Клиент не найден"));
        if (!storedClient.password().equals(password)) {
            throw new BadRequestException("Некорректные учетные данные");
        }
        clientRepository.deleteByLogin(login);
    }

    private void validateCredentials(String login, String password) throws BadRequestException {
        if (isBlank(login) || isBlank(password)) {
            throw new BadRequestException("Логин и пароль обязательны");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
