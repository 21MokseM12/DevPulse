package backend.academy.bot.service.managers.stateless;

import backend.academy.bot.commands.Command;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.service.ScrapperConnectionService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.connectivity.LinkResponse;

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
            List<LinkResponse> links = scrapperConnectionService.getAllLinks(update.message().chat().id());
            if (links.isEmpty()) {
                return new SendMessage(update.message().chat().id(), Messages.EMPTY_LINK_LIST.toString());
            }
            StringBuilder reply = new StringBuilder(HEADER_MESSAGE);
            links.forEach(link -> reply.append(link.url()).append("\n"));
            return new SendMessage(update.message().chat().id(), reply.toString());
        } catch (BadRequestException e) {
            return new SendMessage(update.message().chat().id(), e.getMessage());
        }
    }

    @Override
    public Command getCommand() {
        return listCommand;
    }
}
