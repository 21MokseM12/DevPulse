package backend.academy.scrapper.service.notifications.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.DeliveryMode;
import backend.academy.scrapper.config.properties.CommonKafkaProperties;
import backend.academy.scrapper.db.model.KafkaOutboxMessage;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import scrapper.bot.connectivity.model.LinkUpdate;

@ExtendWith(MockitoExtension.class)
class KafkaOutboxProcessorTest {

    @Mock
    private KafkaOutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaOutboxProcessor processor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ScrapperConfig scrapperConfig = new ScrapperConfig(
                new ScrapperConfig.GitHubCredentials("token", "https://api.github.com"),
                new ScrapperConfig.StackOverflowCredentials("https://api.stackexchange.com", "key", "token"),
                "http://localhost:8080",
                new ScrapperConfig.SchedulerCredentials(
                        java.time.Duration.ofSeconds(15), java.time.Duration.ofSeconds(30), 4),
                new ScrapperConfig.OutboxCredentials("link-updates", java.time.Duration.ofSeconds(5), 100),
                new ScrapperConfig.DeliveryCredentials(DeliveryMode.KAFKA),
                new ScrapperConfig.AuthCredentials("X-Internal-Secret", "test-secret"));
        CommonKafkaProperties kafkaProperties = new CommonKafkaProperties(
                "localhost:9092",
                new CommonKafkaProperties.ConsumerProperties(
                        false, org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL, "earliest", "*"),
                new CommonKafkaProperties.ProducerProperties("test-client", "all", true),
                new CommonKafkaProperties.RetryPolicyProperties(1, 1.0, 1, 2, true));
        processor = new KafkaOutboxProcessor(
                outboxRepository,
                kafkaTemplate,
                objectMapper,
                scrapperConfig,
                kafkaProperties,
                Clock.systemUTC(),
                new SimpleMeterRegistry());
    }

    @Test
    void processBatch_onSuccess_marksMessageAsSent() throws Exception {
        String payload = objectMapper.writeValueAsString(new LinkUpdate(
                1L,
                URI.create("https://github.com/acme/repo"),
                URI.create("https://github.com/acme/repo/commit/1"),
                "title",
                "owner",
                "desc",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                List.of("alice")));
        when(outboxRepository.findPendingBatch(100))
                .thenReturn(List.of(new KafkaOutboxMessage(10L, "link-updates", payload)));
        when(kafkaTemplate.send(eq("link-updates"), any())).thenReturn(CompletableFuture.completedFuture(null));
        when(outboxRepository.countPending()).thenReturn(0L);

        processor.processBatch();

        verify(outboxRepository, times(1)).markSent(eq(10L), any(), eq(1));
        verify(outboxRepository, times(0)).incrementAttemptCount(anyLong(), anyInt());
    }

    @Test
    void processBatch_onKafkaFailure_incrementsAttemptCountByRetryCount() throws Exception {
        String payload = objectMapper.writeValueAsString(new LinkUpdate(
                1L,
                URI.create("https://github.com/acme/repo"),
                URI.create("https://github.com/acme/repo/commit/1"),
                "title",
                "owner",
                "desc",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                List.of("alice")));
        when(outboxRepository.findPendingBatch(100))
                .thenReturn(List.of(new KafkaOutboxMessage(10L, "link-updates", payload)));
        when(kafkaTemplate.send(eq("link-updates"), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));
        when(outboxRepository.countPending()).thenReturn(1L);

        processor.processBatch();

        verify(kafkaTemplate, times(2)).send(eq("link-updates"), any());
        verify(outboxRepository, times(1)).incrementAttemptCount(10L, 2);
        verify(outboxRepository, times(0)).markSent(anyLong(), any(), anyInt());
    }

    @Test
    void processBatch_afterFailureThenSuccess_marksSentOnNextRun() throws Exception {
        String payload = objectMapper.writeValueAsString(new LinkUpdate(
                1L,
                URI.create("https://github.com/acme/repo"),
                URI.create("https://github.com/acme/repo/commit/1"),
                "title",
                "owner",
                "desc",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                List.of("alice")));
        KafkaOutboxMessage message = new KafkaOutboxMessage(10L, "link-updates", payload);
        when(outboxRepository.findPendingBatch(100)).thenReturn(List.of(message), List.of(message));
        when(kafkaTemplate.send(eq("link-updates"), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(outboxRepository.countPending()).thenReturn(1L, 0L);

        processor.processBatch();
        processor.processBatch();

        verify(outboxRepository, times(1)).incrementAttemptCount(10L, 2);
        verify(outboxRepository, times(1)).markSent(eq(10L), any(), eq(1));
    }

    @Test
    void processBatch_inHttpMode_skipsOutboxPublishing() {
        ScrapperConfig httpModeConfig = new ScrapperConfig(
                new ScrapperConfig.GitHubCredentials("token", "https://api.github.com"),
                new ScrapperConfig.StackOverflowCredentials("https://api.stackexchange.com", "key", "token"),
                "http://localhost:8080",
                new ScrapperConfig.SchedulerCredentials(
                        java.time.Duration.ofSeconds(15), java.time.Duration.ofSeconds(30), 4),
                new ScrapperConfig.OutboxCredentials("link-updates", java.time.Duration.ofSeconds(5), 100),
                new ScrapperConfig.DeliveryCredentials(DeliveryMode.HTTP),
                new ScrapperConfig.AuthCredentials("X-Internal-Secret", "test-secret"));
        CommonKafkaProperties kafkaProperties = new CommonKafkaProperties(
                "localhost:9092",
                new CommonKafkaProperties.ConsumerProperties(
                        false, org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL, "earliest", "*"),
                new CommonKafkaProperties.ProducerProperties("test-client", "all", true),
                new CommonKafkaProperties.RetryPolicyProperties(1, 1.0, 1, 2, true));
        KafkaOutboxProcessor httpModeProcessor = new KafkaOutboxProcessor(
                outboxRepository,
                kafkaTemplate,
                objectMapper,
                httpModeConfig,
                kafkaProperties,
                Clock.systemUTC(),
                new SimpleMeterRegistry());

        httpModeProcessor.processBatch();

        verify(outboxRepository, times(0)).findPendingBatch(anyInt());
        verify(kafkaTemplate, times(0)).send(any(), any());
    }
}
