package backend.academy.bot.service.requests.mapper.impl;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.TrackRequest;
import backend.academy.bot.service.commands.impl.stateful.TrackCommand;
import backend.academy.bot.service.requests.mapper.RequestMapper;
import backend.academy.bot.service.commands.impl.stateful.sessions.TrackSessionManager;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackRequestMapper implements RequestMapper {

    private final TrackSessionManager trackSessionManager;

    private final TrackCommand trackCommand;

    @Autowired
    public TrackRequestMapper(
        TrackSessionManager trackSessionManager,
        TrackCommand trackCommand
    ) {
        this.trackSessionManager = trackSessionManager;
        this.trackCommand = trackCommand;
    }

    @Override
    public Request map(Update update) {
        return new TrackRequest(update.message().chat().id(), update.message().text());
    }

    @Override
    public boolean canMap(Update update) {
        return trackSessionManager.hasSession(update.message().chat().id())
            || update.message().text().equals(trackCommand.apiCommand());
    }
}
