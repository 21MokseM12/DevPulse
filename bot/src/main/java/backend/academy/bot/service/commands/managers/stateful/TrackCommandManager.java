package backend.academy.bot.service.commands.managers.stateful;

import backend.academy.bot.service.commands.Command;
import backend.academy.bot.enums.Messages;
import backend.academy.bot.enums.TrackCommandStates;
import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.model.entity.LinkDTO;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.utils.LinkSettingsParser;
import backend.academy.bot.utils.LinkValidator;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Component
public class TrackCommandManager implements StatefulCommandManager {

    private static final Map<Long, LinkDTO> STATES;

    @Autowired
    @Qualifier("trackCommand")
    private Command trackCommand;

    @Autowired
    private ScrapperConnectionService scrapperConnectionService;

    static {
        STATES = new HashMap<>();
    }

    @Override
    public SendMessage createReply(Update update) {
        if (!STATES.containsKey(update.message().chat().id())) {
            STATES.put(update.message().chat().id(), new LinkDTO(TrackCommandStates.LINK));
            return new SendMessage(update.message().chat().id(), TrackCommandStates.LINK.successMessage());
        } else {
            LinkDTO linkDTO = STATES.get(update.message().chat().id());
            switch (linkDTO.state()) {
                case LINK:
                    if (!LinkValidator.isValid(update.message().text())) {
                        return new SendMessage(
                                update.message().chat().id(), linkDTO.state().errorMessage());
                    }
                    linkDTO.state(TrackCommandStates.TAGS);
                    linkDTO.uri(update.message().text());
                    return new SendMessage(
                            update.message().chat().id(), linkDTO.state().successMessage());
                case TAGS:
                    linkDTO.state(TrackCommandStates.FILTERS);
                    linkDTO.tags(
                            LinkSettingsParser.parseSettings(update.message().text()));
                    return new SendMessage(
                            update.message().chat().id(), linkDTO.state().successMessage());
                case FILTERS:
                    try {
                        linkDTO.filters(LinkSettingsParser.parseSettings(
                                update.message().text()));
                        scrapperConnectionService.subscribeLink(
                                update.message().chat().id(), linkDTO);
                        STATES.remove(update.message().chat().id());
                        return new SendMessage(
                                update.message().chat().id(), Messages.SUCCESS_SUBSCRIBE_LINK.toString());
                    } catch (BadRequestException e) {
                        return new SendMessage(update.message().chat().id(), e.getMessage());
                    }
                default:
                    throw new InvalidCommandException(Messages.ERROR.toString());
            }
        }
    }

    @Override
    public boolean hasState(long chatId) {
        return STATES.containsKey(chatId);
    }

    @Override
    public Command getCommand() {
        return trackCommand;
    }
}
