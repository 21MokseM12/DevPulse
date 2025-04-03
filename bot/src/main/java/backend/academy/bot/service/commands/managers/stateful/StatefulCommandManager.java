package backend.academy.bot.service.commands.managers.stateful;

import backend.academy.bot.service.commands.managers.CommandManager;

public interface StatefulCommandManager extends CommandManager {

    boolean hasState(long chatId);
}
