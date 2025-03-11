package backend.academy.bot.factory;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.commands.managers.CommandManager;

public interface CommandManagerFactory {
    CommandManager get(Request request);
}
