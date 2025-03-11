package backend.academy.bot.service.managers.stateless;

import backend.academy.bot.commands.Command;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.service.ScrapperConnectionService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Component
// todo добавить вывод сообщения о уже выполненной регистрации (опционально)
public class StartCommandManager implements StatelessCommandManager {

    @Autowired
    private ScrapperConnectionService scrapperConnectionService;

    @Autowired
    @Qualifier("startCommand")
    private Command startCommand;

    @Override
    public SendMessage createReply(Update update) {
        try {
            scrapperConnectionService.registerChat(update.message().chat().id());
            return new SendMessage(update.message().chat().id(), Messages.WELCOME_MESSAGE.toString());
        } catch (BadRequestException e) {
            return new SendMessage(update.message().chat().id(), e.getMessage());
        }
    }

    @Override
    public Command getCommand() {
        return startCommand;
    }
}
