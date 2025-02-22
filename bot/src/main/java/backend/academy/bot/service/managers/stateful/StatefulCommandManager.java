package backend.academy.bot.service.managers.stateful;

import backend.academy.bot.service.managers.CommandManager;

public interface StatefulCommandManager extends CommandManager {

    boolean hasState(long chatId);
}
