package backend.academy.bot.service.commands.managers.stateless;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.Command;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Component
public class ListCommandManager implements StatelessCommandManager {

    private static final String HEADER_MESSAGE = "Список отслеживаемых ссылок:\n";

    private final Command listCommand;

    private final ScrapperConnectionService scrapperConnectionService;

    @Autowired
    public ListCommandManager(
        @Qualifier("listCommand") Command listCommand,
        ScrapperConnectionService scrapperConnectionService
    ) {
        this.listCommand = listCommand;
        this.scrapperConnectionService = scrapperConnectionService;
    }

    @Override
    public SendMessage createReply(Request request) {
        try {
            List<LinkResponse> links = scrapperConnectionService.getAllLinks(request.getChatId());
            if (links.isEmpty()) {
                return new SendMessage(request.getChatId(), Messages.EMPTY_LINK_LIST.toString());
            }
            StringBuilder reply = new StringBuilder(HEADER_MESSAGE);
            links.forEach(link -> reply.append(link.url()).append("\n"));
            return new SendMessage(request.getChatId(), reply.toString());
        } catch (BadRequestException e) {
            return new SendMessage(request.getChatId(), e.getMessage());
        }
    }

    @Override
    public Command getCommand() {
        return listCommand;
    }
}
