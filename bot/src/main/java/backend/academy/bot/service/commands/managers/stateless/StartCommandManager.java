package backend.academy.bot.service.commands.managers.stateless;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.Command;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Component
// todo добавить вывод сообщения о уже выполненной регистрации (опционально)
public class StartCommandManager implements StatelessCommandManager {

    private final ScrapperConnectionService scrapperConnectionService;

    private final Command startCommand;

    @Autowired
    public StartCommandManager(
            @Qualifier("startCommand") Command startCommand, ScrapperConnectionService scrapperConnectionService) {
        this.scrapperConnectionService = scrapperConnectionService;
        this.startCommand = startCommand;
    }

    @Override
    public SendMessage createReply(Request request) {
        try {
            scrapperConnectionService.registerChat(request.getChatId());
            return new SendMessage(request.getChatId(), Messages.WELCOME_MESSAGE.toString());
        } catch (BadRequestException e) {
            return new SendMessage(request.getChatId(), e.getMessage());
        }
    }

    @Override
    public Command getCommand() {
        return startCommand;
    }
}
