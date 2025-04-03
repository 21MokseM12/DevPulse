package backend.academy.bot.service.requests.mapper.impl;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.model.requests.UntrackRequest;
import backend.academy.bot.service.commands.impl.stateful.UntrackCommand;
import backend.academy.bot.service.commands.impl.stateful.sessions.UntrackSessionManager;
import backend.academy.bot.service.requests.mapper.RequestMapper;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UntrackRequestMapper implements RequestMapper {

    private final UntrackSessionManager untrackSessionManager;

    private final UntrackCommand untrackCommand;

    @Autowired
    public UntrackRequestMapper(UntrackSessionManager untrackSessionManager, UntrackCommand untrackCommand) {
        this.untrackSessionManager = untrackSessionManager;
        this.untrackCommand = untrackCommand;
    }

    @Override
    public Request map(Update update) {
        if (update.callbackQuery() != null) {
            String callback = update.callbackQuery().data();
            return new UntrackRequest(Long.parseLong(callback.split("_")[0]), callback, true);
        } else if (update.message() != null) {
            return new UntrackRequest(
                    update.message().chat().id(), update.message().text(), false);
        }
        return null;
    }

    @Override
    public boolean canMap(Update update) {
        if (update.message() != null) {
            if (update.message().text().equals(untrackCommand.apiCommand())) {
                return true;
            } else {
                return untrackSessionManager.hasSession(update.message().chat().id());
            }
        } else {
            return update.callbackQuery() != null
                    && untrackSessionManager.hasSession(
                            Long.parseLong(update.callbackQuery().data().split("_")[0]));
        }
    }
}
