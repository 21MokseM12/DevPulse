package backend.academy.scrapper.config.access;

import backend.academy.scrapper.config.DatabaseConfig;
import backend.academy.scrapper.database.ChatService;
import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.orm.OrmChatService;
import backend.academy.scrapper.database.orm.OrmLinkService;
import backend.academy.scrapper.database.orm.repository.OrmChatRepository;
import backend.academy.scrapper.database.orm.repository.OrmFilterRepository;
import backend.academy.scrapper.database.orm.repository.OrmLinkRepository;
import backend.academy.scrapper.database.orm.repository.OrmProcessedIdsRepository;
import backend.academy.scrapper.database.orm.repository.OrmTagRepository;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "ORM")
public class OrmAccessConfig {

    private final DatabaseConfig databaseConfig;

    private final Clock clock;

    @Autowired
    public OrmAccessConfig(DatabaseConfig databaseConfig, Clock clock) {
        this.databaseConfig = databaseConfig;
        this.clock = clock;
    }

    @Bean
    public LinkService linkService(
            OrmLinkRepository ormLinkRepository,
            OrmChatRepository ormChatRepository,
            OrmProcessedIdsRepository ormProcessedIdsRepository,
            OrmTagRepository ormTagRepository,
            OrmFilterRepository ormFilterRepository) {
        return new OrmLinkService(
                clock,
                databaseConfig,
                ormLinkRepository,
                ormChatRepository,
                ormProcessedIdsRepository,
                ormTagRepository,
                ormFilterRepository);
    }

    @Bean
    public ChatService chatService(OrmChatRepository ormChatRepository) {
        return new OrmChatService(ormChatRepository);
    }
}
