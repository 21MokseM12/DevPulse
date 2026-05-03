package backend.academy.scrapper.integration_test.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.config.CommonKafkaConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.properties.CommonKafkaProperties;
import backend.academy.scrapper.db.repository.KafkaOutboxRepository;
import backend.academy.scrapper.integration_test.TestApplication;
import backend.academy.scrapper.integration_test.config.TestContainersConfiguration;
import backend.academy.scrapper.service.notifications.impl.KafkaOutboxProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import scrapper.bot.connectivity.model.LinkUpdate;

@Testcontainers
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false",
        "app.scrapper.github.token=test-token",
        "app.scrapper.stackoverflow.key=test-key",
        "app.scrapper.stackoverflow.access-token=test-access-token",
        "app.scrapper.outbox.topic=link-topic-response",
        "app.scrapper.outbox.interval=1h",
        "app.scrapper.outbox.batch-size=100",
        "kafka.properties.retry-policy.interval=10",
        "kafka.properties.retry-policy.multiplier=1.0",
        "kafka.properties.retry-policy.maxDelay=10",
        "kafka.properties.retry-policy.max-attempts=2"
})
class KafkaOutboxProcessorIntegrationTest extends TestApplication {

    @Autowired
    private KafkaOutboxRepository outboxRepository;

    @Autowired
    private KafkaOutboxProcessor outboxProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScrapperConfig scrapperConfig;

    @Autowired
    private CommonKafkaProperties commonKafkaProperties;

    @Autowired
    private Clock clock;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    @Qualifier(CommonKafkaConfig.COMMON_KAFKA_TEMPLATE)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.scrapper.outbox.topic}")
    private String outboxTopic;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM kafka_outbox");
        ensureTopicExists(outboxTopic);
    }

    @AfterEach
    void tearDown() {
        // Keep shared Kafka container running to avoid bootstrap remapping.
    }

    @Test
    void processBatch_publishesOutboxMessageAndMarksItSent() {
        outboxRepository.save(outboxTopic, createUpdate(101L));

        outboxProcessor.processBatch();

        Integer sentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kafka_outbox WHERE sent = true", Integer.class);
        Integer attemptCount = jdbcTemplate.queryForObject("SELECT attempt_count FROM kafka_outbox LIMIT 1", Integer.class);
        assertThat(sentCount).isEqualTo(1);
        assertThat(attemptCount).isEqualTo(1);

        List<String> values = pollRecordValues(outboxTopic);
        assertThat(values).anySatisfy(value -> assertThat(value).contains("\"id\":101"));
    }

    @Test
    void processBatch_brokerFailureIncrementsAttemptCount() {
        outboxRepository.save(outboxTopic, createUpdate(202L));
        KafkaOutboxProcessor brokenProcessor = createProcessorWithBrokenKafkaBootstrap();

        brokenProcessor.processBatch();

        Integer sentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kafka_outbox WHERE sent = true", Integer.class);
        Integer attemptCount =
                jdbcTemplate.queryForObject("SELECT attempt_count FROM kafka_outbox WHERE sent = false LIMIT 1", Integer.class);
        assertThat(sentCount).isZero();
        assertThat(attemptCount).isEqualTo(2);
    }

    @Test
    void processBatch_afterProcessorRestart_recoversAndMarksSent() {
        outboxRepository.save(outboxTopic, createUpdate(303L));
        KafkaOutboxProcessor brokenProcessor = createProcessorWithBrokenKafkaBootstrap();

        brokenProcessor.processBatch();
        Integer attemptsAfterFailure =
                jdbcTemplate.queryForObject("SELECT attempt_count FROM kafka_outbox WHERE sent = false LIMIT 1", Integer.class);
        assertThat(attemptsAfterFailure).isEqualTo(2);

        KafkaOutboxProcessor restartedProcessor = new KafkaOutboxProcessor(
                outboxRepository,
                kafkaTemplate,
                objectMapper,
                scrapperConfig,
                commonKafkaProperties,
                clock,
                meterRegistry);

        restartedProcessor.processBatch();

        Integer sentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kafka_outbox WHERE sent = true", Integer.class);
        Integer finalAttempts = jdbcTemplate.queryForObject("SELECT attempt_count FROM kafka_outbox LIMIT 1", Integer.class);
        assertThat(sentCount).isEqualTo(1);
        assertThat(finalAttempts).isEqualTo(3);
    }

    private KafkaOutboxProcessor createProcessorWithBrokenKafkaBootstrap() {
        Map<String, Object> producerProps = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:1",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                ProducerConfig.CLIENT_ID_CONFIG, "broken-outbox-test-producer");
        KafkaTemplate<String, Object> brokenKafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerProps));
        return new KafkaOutboxProcessor(
                outboxRepository,
                brokenKafkaTemplate,
                objectMapper,
                scrapperConfig,
                commonKafkaProperties,
                clock,
                meterRegistry);
    }

    private LinkUpdate createUpdate(long id) {
        return new LinkUpdate(
                id,
                URI.create("https://github.com/acme/repo/" + id),
                "title-" + id,
                "owner",
                "desc",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                List.of(1L));
    }

    private List<String> pollRecordValues(String topic) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestContainersConfiguration.kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "outbox-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()))) {
            consumer.subscribe(List.of(topic));
            for (int i = 0; i < 5; i++) {
                var records = consumer.poll(Duration.ofSeconds(2));
                if (!records.isEmpty()) {
                    List<String> values = new java.util.ArrayList<>();
                    for (ConsumerRecord<String, String> record : records) {
                        values.add(record.value());
                    }
                    return values;
                }
            }
            return List.of();
        }
    }

    private void ensureTopicExists(String topic) {
        int maxAttempts = 20;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (AdminClient adminClient = AdminClient.create(Map.of(
                    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, TestContainersConfiguration.kafka.getBootstrapServers()))) {
                try {
                    adminClient.createTopics(List.of(new NewTopic(topic, 1, (short) 1))).all().get(5, TimeUnit.SECONDS);
                } catch (Exception ignored) {
                    // Topic may already exist.
                }
                if (adminClient.listTopics().names().get(5, TimeUnit.SECONDS).contains(topic)) {
                    return;
                }
            } catch (Exception ignored) {
                // Kafka might still be starting.
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for Kafka topic creation", e);
            }
        }
        throw new IllegalStateException("Topic was not created in time: " + topic);
    }
}
