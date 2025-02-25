package backend.academy.bot.service.managers.stateful;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.enums.TrackCommandStates;
import backend.academy.bot.model.LinkDTO;
import backend.academy.bot.model.commands.Command;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.validators.LinkValidator;
import backend.academy.bot.utils.LinkSettingsParser;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TrackCommandManager implements StatefulCommandManager {

    private static final Map<Long, LinkDTO> states;

    @Autowired
    @Qualifier("trackCommand")
    private Command trackCommand;

    @Autowired
    private ScrapperConnectionService scrapperConnectionService;

    @Autowired
    private LinkValidator linkValidator;

    static {
        states = new HashMap<>();
    }

    @Override
    public SendMessage createReply(Update update) {
        if (!states.containsKey(update.message().chat().id())) {
            states.put(
                update.message().chat().id(),
                new LinkDTO(TrackCommandStates.LINK)
            );
            return new SendMessage(
                update.message().chat().id(),
                TrackCommandStates.LINK.successMessage()
            );
        } else {
            LinkDTO linkDTO = states.get(update.message().chat().id());
            switch (linkDTO.state()) {
                case LINK:
                    if (!linkValidator.validLink(update.message().text())) {
                        return new SendMessage(
                            update.message().chat().id(),
                            linkDTO.state().errorMessage()
                        );
                    }
                    linkDTO.state(TrackCommandStates.TAGS);
                    linkDTO.link(update.message().text());
                    return new SendMessage(
                        update.message().chat().id(),
                        linkDTO.state().successMessage()
                    );
                case TAGS:
                    linkDTO.state(TrackCommandStates.FILTERS);
                    linkDTO.tags(LinkSettingsParser.parseSettings(update.message().text()));
                    return new SendMessage(
                        update.message().chat().id(),
                        linkDTO.state().successMessage()
                    );
                case FILTERS:
                    linkDTO.filters(LinkSettingsParser.parseSettings(update.message().text()));
                    scrapperConnectionService.subscribeLink(linkDTO);
                    states.remove(update.message().chat().id());
                    return new SendMessage(
                        update.message().chat().id(),
                        Messages.SUCCESS_SUBSCRIBE_LINK.toString()
                    );
                default: throw new IllegalCallerException(Messages.ERROR.toString());
            }
        }
    }

    @Override
    public boolean hasState(long chatId) {
        return states.containsKey(chatId);
    }

    @Override
    public Command getCommand() {
        return trackCommand;
    }
}
