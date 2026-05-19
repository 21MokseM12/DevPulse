package backend.academy.bot.service.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.db.model.Client;
import backend.academy.bot.db.model.PushPlatform;
import backend.academy.bot.db.model.PushToken;
import backend.academy.bot.db.model.PushTokenStatus;
import backend.academy.bot.db.repository.ClientRepository;
import backend.academy.bot.db.repository.PushTokenRepository;
import backend.academy.bot.exceptions.ChatNotFoundException;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PushTokenServiceTest {
    private PushTokenRepository pushTokenRepository;
    private ClientRepository clientRepository;
    private PushTokenService service;

    @BeforeEach
    void setUp() {
        pushTokenRepository = Mockito.mock(PushTokenRepository.class);
        clientRepository = Mockito.mock(ClientRepository.class);
        service = new PushTokenService(pushTokenRepository, clientRepository);
    }

    @Test
    void registerOrUpdate_isIdempotentWithRepositoryUpsert() {
        PushToken token = new PushToken(
                1L,
                "alice",
                PushPlatform.ANDROID,
                "token-1234567890123456",
                "1.0.0",
                "device-1",
                PushTokenStatus.ACTIVE,
                OffsetDateTime.parse("2026-05-01T00:00:00Z"),
                OffsetDateTime.parse("2026-05-01T00:01:00Z"),
                OffsetDateTime.parse("2026-05-01T00:01:00Z"));
        when(clientRepository.findByLogin("alice")).thenReturn(Optional.of(new Client(1L, "alice", "hash")));
        when(pushTokenRepository.upsert("alice", PushPlatform.ANDROID, "token-1234567890123456", "1.0.0", "device-1"))
                .thenReturn(token);

        PushToken saved =
                service.registerOrUpdate("alice", PushPlatform.ANDROID, "token-1234567890123456", "1.0.0", "device-1");

        assertThat(saved).isEqualTo(token);
        verify(pushTokenRepository)
                .upsert("alice", PushPlatform.ANDROID, "token-1234567890123456", "1.0.0", "device-1");
    }

    @Test
    void deactivate_forMissingClientFailsAuthorization() {
        when(clientRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate("ghost", PushPlatform.ANDROID, "token-1234567890123456"))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void markInvalid_updatesTokenStatus() {
        PushToken token = new PushToken(
                10L,
                "alice",
                PushPlatform.ANDROID,
                "token-1234567890123456",
                null,
                null,
                PushTokenStatus.ACTIVE,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now());
        when(pushTokenRepository.markInvalid(10L)).thenReturn(true);

        service.markInvalid(token);

        verify(pushTokenRepository).markInvalid(10L);
    }
}
