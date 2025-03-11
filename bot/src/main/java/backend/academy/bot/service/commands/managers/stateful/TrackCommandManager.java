package backend.academy.bot.service.commands.managers.stateful;

import backend.academy.bot.enums.Messages;
import backend.academy.bot.enums.TrackCommandStates;
import backend.academy.bot.exceptions.InvalidCommandException;
import backend.academy.bot.model.entity.LinkDTO;
import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.TrackRequest;
import backend.academy.bot.service.ScrapperConnectionService;
import backend.academy.bot.service.commands.Command;
import backend.academy.bot.service.commands.impl.stateful.sessions.TrackSessionManager;
import backend.academy.bot.utils.LinkSettingsParser;
import backend.academy.bot.utils.LinkValidator;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Component
public class TrackCommandManager implements StatefulCommandManager {

    private final Command trackCommand;

    private final ScrapperConnectionService scrapperConnectionService;

    private final TrackSessionManager trackSessionManager;

    private static final Map<Long, LinkDTO> trackLinks = new HashMap<>();

    @Autowired
    public TrackCommandManager(
            @Qualifier("trackCommand") Command trackCommand,
            ScrapperConnectionService scrapperConnectionService,
            TrackSessionManager trackSessionManager) {
        this.trackCommand = trackCommand;
        this.scrapperConnectionService = scrapperConnectionService;
        this.trackSessionManager = trackSessionManager;
    }

    @Override
    public SendMessage createReply(Request request) {
        if (!(request instanceof TrackRequest trackRequest)) {
            return new SendMessage(request.getChatId(), Messages.ERROR.toString());
        } else {
            if (!trackSessionManager.hasSession(trackRequest.getChatId())) {
                trackSessionManager.createSession(trackRequest.getChatId());
                trackLinks.put(trackRequest.getChatId(), new LinkDTO());
                return new SendMessage(trackRequest.getChatId(), TrackCommandStates.LINK.successMessage());
            } else {
                TrackCommandStates state = trackSessionManager.getSession(request.getChatId());
                switch (state) {
                    case LINK:
                        if (!LinkValidator.isValid(trackRequest.getData())) {
                            return new SendMessage(request.getChatId(), state.errorMessage());
                        }
                        trackSessionManager.updateSession(trackRequest.getChatId(), TrackCommandStates.TAGS);
                        trackLinks.get(trackRequest.getChatId()).uri(trackRequest.getData());
                        return new SendMessage(trackRequest.getChatId(), state.successMessage());
                    case TAGS:
                        trackSessionManager.updateSession(trackRequest.getChatId(), TrackCommandStates.FILTERS);
                        trackLinks
                                .get(trackRequest.getChatId())
                                .tags(LinkSettingsParser.parseSettings(trackRequest.getData()));
                        return new SendMessage(trackRequest.getChatId(), state.successMessage());
                    case FILTERS:
                        try {
                            trackLinks
                                    .get(trackRequest.getChatId())
                                    .filters(LinkSettingsParser.parseSettings(trackRequest.getData()));
                            scrapperConnectionService.subscribeLink(
                                    trackRequest.getChatId(), trackLinks.get(trackRequest.getChatId()));
                            trackSessionManager.deleteSession(trackRequest.getChatId());
                            return new SendMessage(
                                    trackRequest.getChatId(), Messages.SUCCESS_SUBSCRIBE_LINK.toString());
                        } catch (BadRequestException e) {
                            return new SendMessage(trackRequest.getChatId(), e.getMessage());
                        }
                    default:
                        throw new InvalidCommandException(Messages.ERROR.toString());
                }
            }
        }
    }

    @Override
    public boolean hasState(long chatId) {
        return trackSessionManager.hasSession(chatId);
    }

    @Override
    public Command getCommand() {
        return trackCommand;
    }
}
