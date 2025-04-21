package backend.academy.bot.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkSettingsParser {

    public static Set<String> parseSettings(final String settings) {
        return Arrays.stream(settings.split(" ")).collect(Collectors.toSet());
    }
}
