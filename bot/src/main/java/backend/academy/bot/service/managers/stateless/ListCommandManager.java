package backend.academy.bot.service.managers.stateless;

import backend.academy.bot.exceptions.BadRequestException;
import backend.academy.bot.exceptions.RequestSendException;
import backend.academy.bot.model.Link;
import backend.academy.bot.model.commands.Command;
import backend.academy.bot.service.ScrapperConnectionService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ListCommandManager implements StatelessCommandManager {

    private static final String HEADER_MESSAGE = "Список отслеживаемых ссылок:\n";

    @Autowired
    @Qualifier("listCommand")
    private Command listCommand;

    @Autowired
    private ScrapperConnectionService scrapperConnectionService;

    @Override
    public SendMessage createReply(Update update) {
        try {
            StringBuilder reply = new StringBuilder(HEADER_MESSAGE);
            List<Link> links = scrapperConnectionService.getAllLinks(update.message().chat().id());
            links.forEach(link -> reply.append(link.link()).append("\n"));
            return new SendMessage(update.message().chat().id(), reply.toString());
        } catch (BadRequestException | RequestSendException e) {
            return new SendMessage(update.message().chat().id(), e.getMessage());
        }
    }

    @Override
    public Command getCommand() {
        return listCommand;
    }
}
