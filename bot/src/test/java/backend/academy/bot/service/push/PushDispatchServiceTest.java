package backend.academy.bot.service.push;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.config.properties.PushProperties;
import backend.academy.bot.db.model.PushPlatform;
import backend.academy.bot.db.model.PushToken;
import backend.academy.bot.db.model.PushTokenStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import scrapper.bot.connectivity.model.LinkUpdate;

class PushDispatchServiceTest {
    private PushTokenService pushTokenService;
    private PushSender pushSender;
    private PushPayloadBuilder payloadBuilder;
    private PushDispatchService service;

    @BeforeEach
    void setUp() {
        pushTokenService = Mockito.mock(PushTokenService.class);
        pushSender = Mockito.mock(PushSender.class);
        payloadBuilder = Mockito.mock(PushPayloadBuilder.class);
        PushProperties properties = new PushProperties(
                new PushProperties.FcmProperties("http://localhost:9999/fcm", "test-key"),
                new PushProperties.RetryProperties(3, 1, 2.0, 10),
                60);
        service = new PushDispatchService(
                pushTokenService, pushSender, payloadBuilder, properties, new SimpleMeterRegistry());
    }

    @Test
    void dispatchForUpdate_marksTokenInvalidWhenFcmReturnsInvalid() {
        LinkUpdate update = new LinkUpdate(
                100L,
                URI.create("https://github.com/org/repo/issues/1"),
                URI.create("https://github.com/org/repo/issues/1"),
                "Issue updated",
                "octocat",
                "Description",
                OffsetDateTime.parse("2026-05-01T00:00:00Z"),
                List.of("alice"));
        PushMessagePayload payload = new PushMessagePayload(
                "200",
                "Issue updated",
                "Description",
                "https://github.com/org/repo/issues/1",
                "2026-05-01T00:00:00Z",
                "octocat",
                java.util.Map.of("event_id", "200"));
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
        when(payloadBuilder.build(200L, update)).thenReturn(payload);
        when(pushTokenService.findActiveByClientLogin("alice")).thenReturn(List.of(token));
        when(pushSender.send("token-1234567890123456", payload))
                .thenReturn(PushDeliveryResult.invalidToken("UNREGISTERED"));

        service.dispatchForUpdate(200L, update);

        verify(pushTokenService).markInvalid(token);
    }

    @Test
    void dispatchForUpdate_retriesTransientFailuresAndStopsAfterSuccess() {
        LinkUpdate update = new LinkUpdate(
                100L,
                URI.create("https://github.com/org/repo/issues/1"),
                URI.create("https://github.com/org/repo/issues/1"),
                "Issue updated",
                "octocat",
                "Description",
                OffsetDateTime.parse("2026-05-01T00:00:00Z"),
                List.of("alice"));
        PushMessagePayload payload = new PushMessagePayload(
                "200",
                "Issue updated",
                "Description",
                "https://github.com/org/repo/issues/1",
                "2026-05-01T00:00:00Z",
                "octocat",
                java.util.Map.of("event_id", "200"));
        PushToken token = new PushToken(
                11L,
                "alice",
                PushPlatform.ANDROID,
                "token-1234567890123456",
                null,
                null,
                PushTokenStatus.ACTIVE,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now());
        when(payloadBuilder.build(200L, update)).thenReturn(payload);
        when(pushTokenService.findActiveByClientLogin("alice")).thenReturn(List.of(token));
        when(pushSender.send("token-1234567890123456", payload))
                .thenReturn(PushDeliveryResult.transientFailure("Unavailable"))
                .thenReturn(PushDeliveryResult.success());

        service.dispatchForUpdate(200L, update);

        verify(pushSender, Mockito.times(2)).send("token-1234567890123456", payload);
        verify(pushTokenService, never()).markInvalid(token);
    }
}
