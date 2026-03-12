package backend.academy.scrapper.integration_test.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers
public class TestContainersConfiguration {

    private static final String MIGRATIONS_PATH = "scrapper/src/main/resources/db/migrations/";
    private static final String MIGRATIONS_FILE_NAME = "master.xml";

    private static final List<String> KAFKA_TOPICS = List.of(
        "client-listener-topic-request",
        "link-listener-topic-request",
        "link-topic-response"
    );
    private static final int PARTITIONS = 2;
    private static final short REPLICATION_FACTOR = 1;

    public static PostgreSQLContainer<?> postgres;
    public static KafkaContainer kafka;

    static {
        runPostgres();
        runKafka();
    }

    private static void runPostgres() {
        postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres")
            .withCommand("postgres", "-c", "timezone=UTC");
        postgres.start();
        try {
            runLiquibaseMigrations(postgres);
        } catch (LiquibaseException | SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runKafka() {
        kafka = new KafkaContainer("apache/kafka-native");
        kafka.start();
        configureKafka();
    }

    private static void configureKafka() {
        try (AdminClient adminClient = createAdminClient()) {
            for (String topicName : KAFKA_TOPICS) {
                NewTopic topic = new NewTopic(topicName, PARTITIONS, REPLICATION_FACTOR);
                adminClient.createTopics(java.util.Collections.singletonList(topic)).all().get();
                System.out.println("Created topic: " + topicName);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create Kafka topics", e);
        }
    }

    private static AdminClient createAdminClient() {
        Map<String, Object> config = Map.of(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()
        );
        return AdminClient.create(config);
    }

    private static void runLiquibaseMigrations(PostgreSQLContainer<?> postgres)
        throws FileNotFoundException, SQLException, LiquibaseException {
        Connection connection =
            DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        Database database =
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Path path =
            new File(".").toPath().toAbsolutePath().getParent().getParent().resolve(MIGRATIONS_PATH);
        Liquibase liquibase = new Liquibase(MIGRATIONS_FILE_NAME, new DirectoryResourceAccessor(path), database);
        liquibase.update(new Contexts("test"), new LabelExpression());
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        //Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        //Kafka - both custom config and Spring Kafka (RetryableTopic AdminClient) use these
        String bootstrapServers = kafka.getBootstrapServers();
        registry.add("kafka.properties.bootstrap-servers", () -> bootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);
    }
}
