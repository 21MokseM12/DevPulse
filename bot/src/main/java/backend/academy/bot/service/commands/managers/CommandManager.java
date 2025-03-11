package backend.academy.bot.service.commands.managers;

import backend.academy.bot.model.requests.Request;
import backend.academy.bot.service.commands.Command;
import com.pengrad.telegrambot.request.SendMessage;

public interface CommandManager {

    SendMessage createReply(Request request);

    Command getCommand();
}
