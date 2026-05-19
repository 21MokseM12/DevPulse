package backend.academy.bot.service.push;

import backend.academy.bot.db.model.PushPlatform;
import backend.academy.bot.db.model.PushToken;
import backend.academy.bot.db.repository.ClientRepository;
import backend.academy.bot.db.repository.PushTokenRepository;
import backend.academy.bot.exceptions.ChatNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushTokenService {
    private final PushTokenRepository pushTokenRepository;
    private final ClientRepository clientRepository;

    public PushToken registerOrUpdate(
            String clientLogin, PushPlatform platform, String token, String appVersion, String deviceId) {
        assertClientExists(clientLogin);
        PushToken stored = pushTokenRepository.upsert(clientLogin, platform, token, appVersion, deviceId);
        log.info("Push token registered and activated");
        return stored;
    }

    public boolean deactivate(String clientLogin, PushPlatform platform, String token) {
        assertClientExists(clientLogin);
        boolean updated = pushTokenRepository.deactivate(clientLogin, platform, token);
        if (updated) {
            log.info("Push token deactivated");
        } else {
            log.info("Push token already inactive or missing");
        }
        return updated;
    }

    public void markInvalid(PushToken token) {
        if (pushTokenRepository.markInvalid(token.id())) {
            log.info("Push token marked invalid after FCM response");
        }
    }

    public java.util.List<PushToken> findActiveByClientLogin(String clientLogin) {
        return pushTokenRepository.findActiveByClientLogin(clientLogin);
    }

    public long countActiveByClientLogin(String clientLogin) {
        return pushTokenRepository.countActiveByClientLogin(clientLogin);
    }

    private void assertClientExists(String clientLogin) {
        if (clientRepository.findByLogin(clientLogin).isEmpty()) {
            throw new ChatNotFoundException("Клиент не найден");
        }
    }
}
