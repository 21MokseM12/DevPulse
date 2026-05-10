package backend.academy.bot.service;

import backend.academy.bot.db.model.Client;
import backend.academy.bot.db.repository.ClientRepository;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.exceptions.ClientLifecycleSyncException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientOperationService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ScrapperConnectionService scrapperConnectionService;

    public void registerClient(String login, String password) throws BadRequestException {
        validateCredentials(login, password);
        var storedClient = clientRepository.findByLogin(login);
        if (storedClient.isPresent()) {
            validatePassword(storedClient.orElseThrow(), password);
            return;
        }
        String passwordHash = passwordEncoder.encode(password);
        clientRepository.save(login, passwordHash);
        try {
            scrapperConnectionService.registerChat(login, password);
        } catch (BadRequestException e) {
            rollbackRegistration(login, e);
        }
    }

    public void unregisterClient(String login, String password) throws BadRequestException {
        validateCredentials(login, password);
        var storedClient = clientRepository.findByLogin(login);
        if (storedClient.isEmpty()) {
            unregisterInScrapperIdempotent(login, password);
            return;
        }
        validatePassword(storedClient.orElseThrow(), password);

        boolean deleted = clientRepository.deleteByLogin(login);
        if (!deleted) {
            return;
        }

        try {
            scrapperConnectionService.unregisterChat(login, password);
        } catch (ChatNotFoundException e) {
            log.info("Chat for login {} already removed in scrapper", login);
        } catch (BadRequestException e) {
            restoreClientAfterFailedUnregister(login, storedClient.orElseThrow().passwordHash(), e);
        }
    }

    private void validateCredentials(String login, String password) throws BadRequestException {
        if (isBlank(login) || isBlank(password)) {
            throw new BadRequestException("Логин и пароль обязательны");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validatePassword(Client storedClient, String password) throws BadRequestException {
        String passwordHash = storedClient.passwordHash();
        if (passwordHash == null || passwordHash.isBlank() || !passwordEncoder.matches(password, passwordHash)) {
            throw new BadRequestException("Некорректные учетные данные");
        }
    }

    private void unregisterInScrapperIdempotent(String login, String password) {
        try {
            scrapperConnectionService.unregisterChat(login, password);
        } catch (ChatNotFoundException e) {
            log.info("Idempotent unregister for missing login {} in scrapper", login);
        } catch (BadRequestException e) {
            throw new ClientLifecycleSyncException("Не удалось синхронизировать удаление клиента", e);
        }
    }

    private void rollbackRegistration(String login, BadRequestException cause) {
        boolean deleted = clientRepository.deleteByLogin(login);
        if (!deleted) {
            log.error("Cannot rollback local registration for login {}", login);
        }
        throw new ClientLifecycleSyncException("Не удалось синхронизировать регистрацию клиента", cause);
    }

    private void restoreClientAfterFailedUnregister(String login, String passwordHash, BadRequestException cause) {
        try {
            clientRepository.save(login, passwordHash);
        } catch (RuntimeException restoreError) {
            throw new ClientLifecycleSyncException(
                    "Не удалось синхронизировать удаление клиента и выполнить компенсацию", restoreError);
        }
        throw new ClientLifecycleSyncException(
                "Не удалось синхронизировать удаление клиента, выполнена компенсация", cause);
    }
}
