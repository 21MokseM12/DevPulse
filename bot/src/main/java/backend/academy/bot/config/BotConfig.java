package backend.academy.bot.config;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Autowired
    private List<Command> commands;

    @Bean
    public TelegramBot telegramBot(ApplicationConfig applicationConfig) {
        TelegramBot bot = new TelegramBot(applicationConfig.telegramToken());
        createMenu(bot);
        return bot;
    }

    private void createMenu(TelegramBot bot) {
        bot.execute(new SetMyCommands(
            commands.stream()
                .map(Command::toApiCommand)
                .toArray(BotCommand[]::new)
        ));
    }
}
