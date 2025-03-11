package backend.academy.bot.utils;

import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkSettingsParser {

    public static List<String> parseSettings(final String settings) {
        return Arrays.stream(settings.split(" ")).toList();
    }
}
