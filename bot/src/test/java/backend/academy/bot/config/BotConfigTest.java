package backend.academy.bot.config;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class BotConfigTest {

    @Autowired
    private BotConfig botConfig;

    @MockitoBean
    private ApplicationConfig applicationConfig;

    @MockitoBean
    @Qualifier("helpCommand")
    private Command command;

    @Test
    public void testTelegramBotCreation() {
        when(applicationConfig.telegramToken()).thenReturn("simple_token");

        BotCommand botCommand = mock(BotCommand.class);
        when(command.toApiCommand()).thenReturn(botCommand);

        TelegramBot telegramBot = botConfig.telegramBot(applicationConfig);

        assertThat(telegramBot).isNotNull();
        verify(applicationConfig, times(1)).telegramToken();
        verify(command, times(1)).toApiCommand();
    }
}
