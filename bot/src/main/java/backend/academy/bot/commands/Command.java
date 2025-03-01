package backend.academy.bot.commands;

import com.pengrad.telegrambot.model.BotCommand;

public interface Command {

    String apiCommand();

    String description();

    default BotCommand toApiCommand() {
        return new BotCommand(apiCommand(), description());
    }
}
