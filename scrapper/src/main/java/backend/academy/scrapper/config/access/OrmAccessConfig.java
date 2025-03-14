package backend.academy.scrapper.config.access;

import backend.academy.scrapper.database.ChatService;
import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.orm.OrmChatService;
import backend.academy.scrapper.database.orm.OrmLinkService;
import backend.academy.scrapper.database.orm.repository.OrmChatRepository;
import backend.academy.scrapper.database.orm.repository.OrmLinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "ORM")
public class OrmAccessConfig {

    @Bean
    public LinkService linkService(
        OrmLinkRepository ormLinkRepository,
        OrmChatRepository ormChatRepository
    ) {
        return new OrmLinkService(ormLinkRepository, ormChatRepository);
    }

    @Bean
    public ChatService chatService(OrmChatRepository ormChatRepository) {
        return new OrmChatService(ormChatRepository);
    }
}
