package backend.academy.scrapper.docs;

import java.util.LinkedHashSet;
import java.util.Set;

final class EnvExampleParser {

    private EnvExampleParser() {}

    static Set<String> extractKeys(String content) {
        Set<String> keys = new LinkedHashSet<>();
        for (String rawLine : content.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int delimiterIndex = line.indexOf('=');
            if (delimiterIndex <= 0) {
                continue;
            }

            String key = line.substring(0, delimiterIndex).trim();
            if (!key.isEmpty()) {
                keys.add(key);
            }
        }
        return keys;
    }
}
