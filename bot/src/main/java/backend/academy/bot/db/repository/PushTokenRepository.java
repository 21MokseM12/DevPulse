package backend.academy.bot.db.repository;

import backend.academy.bot.db.model.PushPlatform;
import backend.academy.bot.db.model.PushToken;
import java.util.List;

public interface PushTokenRepository {
    PushToken upsert(String clientLogin, PushPlatform platform, String token, String appVersion, String deviceId);

    boolean deactivate(String clientLogin, PushPlatform platform, String token);

    List<PushToken> findActiveByClientLogin(String clientLogin);

    boolean markInvalid(long tokenId);

    long countActiveByClientLogin(String clientLogin);
}
