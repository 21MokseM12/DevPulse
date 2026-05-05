package backend.academy.bot.service;

import backend.academy.bot.db.repository.ClientRepository;
import backend.academy.bot.exceptions.ChatNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Service
@RequiredArgsConstructor
public class ClientOperationService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerClient(String login, String password) throws BadRequestException {
        validateCredentials(login, password);
        if (clientRepository.findByLogin(login).isPresent()) {
            throw new BadRequestException("Клиент уже зарегистрирован");
        }
        clientRepository.save(login, passwordEncoder.encode(password));
    }

    public void unregisterClient(String login, String password) throws BadRequestException {
        validateCredentials(login, password);
        var storedClient =
                clientRepository.findByLogin(login).orElseThrow(() -> new ChatNotFoundException("Клиент не найден"));
        String passwordHash = storedClient.passwordHash();
        if (passwordHash == null || passwordHash.isBlank() || !passwordEncoder.matches(password, passwordHash)) {
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
