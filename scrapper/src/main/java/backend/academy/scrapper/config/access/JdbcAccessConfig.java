package backend.academy.scrapper.config.access;

import backend.academy.scrapper.config.DatabaseConfig;
import backend.academy.scrapper.database.ChatService;
import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.jdbc.JdbcChatService;
import backend.academy.scrapper.database.jdbc.JdbcLinkService;
import backend.academy.scrapper.database.jdbc.repository.JdbcChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcLinkToChatRepository;
import backend.academy.scrapper.database.jdbc.repository.JdbcProcessedIdRepository;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "SQL")
public class JdbcAccessConfig {

    private final DatabaseConfig config;

    private final Clock clock;

    @Autowired
    public JdbcAccessConfig(DatabaseConfig config, Clock clock) {
        this.config = config;
        this.clock = clock;
    }

    @Bean
    public LinkService linkService(
            JdbcChatRepository chatRepository,
            JdbcLinkRepository linkRepository,
            JdbcLinkToChatRepository linkToChatRepository,
            JdbcProcessedIdRepository processedIdRepository) {
        return new JdbcLinkService(
                clock, config, chatRepository, linkRepository, linkToChatRepository, processedIdRepository);
    }

    @Bean
    public ChatService chatService(JdbcChatRepository chatRepository, JdbcLinkToChatRepository linkToChatRepository) {
        return new JdbcChatService(chatRepository, linkToChatRepository);
    }
}
