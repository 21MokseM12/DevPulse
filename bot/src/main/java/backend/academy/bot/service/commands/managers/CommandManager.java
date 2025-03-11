package backend.academy.bot.service.commands.managers;

import backend.academy.bot.service.commands.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface CommandManager {

    SendMessage createReply(Update message);

    Command getCommand();
}
