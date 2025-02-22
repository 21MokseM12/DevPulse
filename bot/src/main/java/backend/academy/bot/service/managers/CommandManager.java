package backend.academy.bot.service.managers;

import backend.academy.bot.model.commands.Command;
import com.pengrad.telegrambot.model.Message;

public interface CommandManager {

    String createReply(Message message);

    Command getCommand();
}
