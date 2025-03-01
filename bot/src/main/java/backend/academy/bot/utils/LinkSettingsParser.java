package backend.academy.bot.utils;

import lombok.experimental.UtilityClass;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class LinkSettingsParser {

    public static List<String> parseSettings(final String settings) {
        return Arrays.stream(settings.split(" ")).toList();
    }
}
