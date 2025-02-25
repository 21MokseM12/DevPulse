package backend.academy.bot.service.managers;

import backend.academy.bot.model.commands.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface CommandManager {

    SendMessage createReply(Update message);

    Command getCommand();
}
