package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.db.model.Client;
import backend.academy.bot.db.repository.ClientRepository;
import backend.academy.bot.exceptions.ChatNotFoundException;
import backend.academy.bot.exceptions.ClientLifecycleSyncException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class ClientOperationServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ScrapperConnectionService scrapperConnectionService;

    @InjectMocks
    private ClientOperationService clientOperationService;

    @Test
    void registerClient_registersLocallyAndInScrapper() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        clientOperationService.registerClient("alice", "secret");

        verify(clientRepository).save("alice", "hashed");
        verify(scrapperConnectionService).registerChat("alice", "secret");
    }

    @Test
    void registerClient_existingClientWithSamePassword_isIdempotent() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.of(new Client(1L, "alice", "hashed")));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        clientOperationService.registerClient("alice", "secret");

        verify(clientRepository, never()).save("alice", "hashed");
        verify(scrapperConnectionService, never()).registerChat("alice", "secret");
    }

    @Test
    void registerClient_whenScrapperFails_rollsBackLocalAndThrowsSyncException() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(clientRepository.deleteByLogin("alice")).thenReturn(true);
        doThrow(new BadRequestException("remote failure"))
                .when(scrapperConnectionService)
                .registerChat("alice", "secret");

        ClientLifecycleSyncException exception = assertThrows(
                ClientLifecycleSyncException.class, () -> clientOperationService.registerClient("alice", "secret"));

        assertEquals("Не удалось синхронизировать регистрацию клиента", exception.getMessage());
        verify(clientRepository).save("alice", "hashed");
        verify(clientRepository).deleteByLogin("alice");
    }

    @Test
    void unregisterClient_happyPath_deletesLocalAndScrapper() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.of(new Client(1L, "alice", "hashed")));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(clientRepository.deleteByLogin("alice")).thenReturn(true);

        clientOperationService.unregisterClient("alice", "secret");

        verify(clientRepository).deleteByLogin("alice");
        verify(scrapperConnectionService).unregisterChat("alice", "secret");
    }

    @Test
    void unregisterClient_whenScrapperFails_restoresLocalAndThrowsSyncException() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.of(new Client(1L, "alice", "hashed")));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(clientRepository.deleteByLogin("alice")).thenReturn(true);
        doThrow(new BadRequestException("remote failure"))
                .when(scrapperConnectionService)
                .unregisterChat("alice", "secret");

        ClientLifecycleSyncException exception = assertThrows(
                ClientLifecycleSyncException.class, () -> clientOperationService.unregisterClient("alice", "secret"));

        assertEquals("Не удалось синхронизировать удаление клиента, выполнена компенсация", exception.getMessage());
        verify(clientRepository).save("alice", "hashed");
    }

    @Test
    void unregisterClient_whenClientMissingAndScrapperNotFound_isIdempotent() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.empty());
        doThrow(new ChatNotFoundException("not found"))
                .when(scrapperConnectionService)
                .unregisterChat("alice", "secret");

        clientOperationService.unregisterClient("alice", "secret");

        verify(clientRepository, never()).deleteByLogin("alice");
    }

    @Test
    void unregisterClient_whenClientMissingAndScrapperFails_throwsSyncException() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.empty());
        doThrow(new BadRequestException("remote failure"))
                .when(scrapperConnectionService)
                .unregisterChat("alice", "secret");

        ClientLifecycleSyncException exception = assertThrows(
                ClientLifecycleSyncException.class, () -> clientOperationService.unregisterClient("alice", "secret"));

        assertEquals("Не удалось синхронизировать удаление клиента", exception.getMessage());
    }

    @Test
    void loginClient_whenClientExistsAndPasswordMatches_succeeds() throws BadRequestException {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.of(new Client(1L, "alice", "hashed")));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        clientOperationService.loginClient("alice", "secret");

        verify(clientRepository).findByLogin("alice");
        verify(scrapperConnectionService, never()).registerChat("alice", "secret");
        verify(scrapperConnectionService, never()).unregisterChat("alice", "secret");
    }

    @Test
    void loginClient_whenClientMissing_throwsNotFound() {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.empty());

        ChatNotFoundException exception =
                assertThrows(ChatNotFoundException.class, () -> clientOperationService.loginClient("alice", "secret"));

        assertEquals("Клиент не найден", exception.getMessage());
    }

    @Test
    void loginClient_whenPasswordInvalid_throwsBadRequest() {
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.of(new Client(1L, "alice", "hashed")));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        BadRequestException exception =
                assertThrows(BadRequestException.class, () -> clientOperationService.loginClient("alice", "wrong"));

        assertEquals("Некорректные учетные данные", exception.getMessage());
    }
}
