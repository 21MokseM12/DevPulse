package backend.academy.scrapper.docs;

import java.util.ArrayList;
import java.util.List;

final class DefenseChecklistParser {

    private DefenseChecklistParser() {}

    static List<ChecklistItem> parseChecklistItems(String markdown) {
        List<ChecklistItem> items = new ArrayList<>();
        for (String line : markdown.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("- [")) {
                continue;
            }
            if (trimmed.length() < 7 || trimmed.charAt(4) != ']') {
                continue;
            }

            char status = trimmed.charAt(3);
            if (status != ' ' && status != 'x' && status != 'X') {
                continue;
            }
            String text = trimmed.substring(6).trim();
            if (text.isEmpty()) {
                continue;
            }
            items.add(new ChecklistItem(status == 'x' || status == 'X', text));
        }
        return items;
    }

    static List<String> extractBacktickPaths(String text) {
        List<String> paths = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            int start = text.indexOf('`', index);
            if (start < 0) {
                break;
            }
            int end = text.indexOf('`', start + 1);
            if (end < 0) {
                break;
            }

            String candidate = text.substring(start + 1, end).trim();
            if (candidate.contains("/") && candidate.contains(".") && !candidate.contains(" ")) {
                paths.add(candidate);
            }
            index = end + 1;
        }
        return paths;
    }

    record ChecklistItem(boolean checked, String text) {}
}
