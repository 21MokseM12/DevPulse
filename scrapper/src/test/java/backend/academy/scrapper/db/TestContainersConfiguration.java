package backend.academy.scrapper.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class TestContainersConfiguration {

    public static PostgreSQLContainer<?> postgres;

    static {
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

    private static void runLiquibaseMigrations(PostgreSQLContainer<?> postgres)
        throws FileNotFoundException, SQLException, LiquibaseException {
        Connection connection =
            DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        Database database =
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Path path =
            new File(".").toPath().toAbsolutePath().getParent().getParent().resolve("migrations");
        Liquibase liquibase = new Liquibase("master.xml", new DirectoryResourceAccessor(path), database);
        liquibase.update(new Contexts("test"), new LabelExpression());
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
