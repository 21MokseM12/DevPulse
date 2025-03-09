package backend.academy.scrapper.config.access;

import backend.academy.scrapper.database.ChatService;
import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.jdbc.JdbcChatService;
import backend.academy.scrapper.database.jdbc.JdbcLinkService;
import backend.academy.scrapper.database.repository.jdbc.JdbcChatRepository;
import backend.academy.scrapper.database.repository.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.database.repository.jdbc.JdbcLinkToChatRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "SQL")
public class JdbcAccessConfig {

    @Bean
    public LinkService linkService(
        JdbcChatRepository chatRepository,
        JdbcLinkRepository linkRepository,
        JdbcLinkToChatRepository linkToChatRepository
    ) {
        return new JdbcLinkService(chatRepository, linkRepository, linkToChatRepository);
    }

    @Bean
    public ChatService chatService(
        JdbcChatRepository chatRepository,
        JdbcLinkToChatRepository linkToChatRepository
    ) {
        return new JdbcChatService(chatRepository, linkToChatRepository);
    }
}
