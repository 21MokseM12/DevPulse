package backend.academy.bot.utils;

import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkSettingsParser {

    public List<String> parseSettings(String text) {
        return Arrays.stream(text.split(" ")).toList();
    }
}
