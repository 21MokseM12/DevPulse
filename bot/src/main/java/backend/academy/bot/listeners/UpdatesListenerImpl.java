package backend.academy.bot.listeners;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class UpdatesListenerImpl implements UpdatesListener {

    @Override
    public int process(List<Update> updates) {
        updates.stream()
            .map(Update::message)
            .forEach(m -> System.out.println(m.text()));
        return CONFIRMED_UPDATES_ALL;
    }
}
