package backend.academy.scrapper.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class TraceabilityMatrixParser {

    private static final Pattern REQUIREMENT_PATTERN = Pattern.compile("F\\d+|NFR\\d+");
    private static final Pattern STATUS_PATTERN = Pattern.compile("(implemented,tested|implemented,residual-risk)");

    private TraceabilityMatrixParser() {}

    static List<TraceabilityRow> parseRows(String markdown) {
        List<TraceabilityRow> rows = new ArrayList<>();
        for (String line : markdown.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("| F") && !trimmed.startsWith("| NFR")) {
                continue;
            }
            String[] columns = trimmed.split("\\|", -1);
            if (columns.length < 6) {
                continue;
            }

            String requirement = columns[1].trim();
            String implementation = columns[2].trim();
            String testEvidence = columns[3].trim();
            String status = columns[4].trim();
            String residualRisk = columns[5].trim();

            if (!REQUIREMENT_PATTERN.matcher(requirement).matches()) {
                continue;
            }
            if (!STATUS_PATTERN.matcher(status).matches()) {
                continue;
            }

            rows.add(new TraceabilityRow(requirement, implementation, testEvidence, status, residualRisk));
        }
        return rows;
    }

    static List<String> extractBacktickPaths(String content) {
        List<String> paths = new ArrayList<>();
        int index = 0;
        while (index < content.length()) {
            int start = content.indexOf('`', index);
            if (start < 0) {
                break;
            }
            int end = content.indexOf('`', start + 1);
            if (end < 0) {
                break;
            }
            String candidate = content.substring(start + 1, end).trim();
            if (!candidate.isEmpty()) {
                paths.add(candidate);
            }
            index = end + 1;
        }
        return paths;
    }

    record TraceabilityRow(
            String requirement, String implementation, String testEvidence, String status, String residualRisk) {}
}
