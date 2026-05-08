package backend.academy.scrapper.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class TestMatrixParser {

    private static final Pattern REQUIREMENT_PATTERN = Pattern.compile("F\\d+");
    private static final Pattern STATUS_PATTERN = Pattern.compile("green|gap");

    private TestMatrixParser() {}

    static List<TestMatrixRow> parseRows(String markdown) {
        List<TestMatrixRow> rows = new ArrayList<>();
        for (String line : markdown.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("| F")) {
                continue;
            }
            String[] columns = trimmed.split("\\|", -1);
            if (columns.length < 6) {
                continue;
            }

            String requirement = columns[1].trim();
            String testEvidence = columns[4].trim();
            String status = columns[5].trim();
            if (!REQUIREMENT_PATTERN.matcher(requirement).matches()) {
                continue;
            }
            if (!STATUS_PATTERN.matcher(status).matches()) {
                continue;
            }

            rows.add(new TestMatrixRow(requirement, testEvidence, status));
        }
        return rows;
    }

    static List<String> extractEvidencePaths(String testEvidence) {
        List<String> paths = new ArrayList<>();
        int index = 0;
        while (index < testEvidence.length()) {
            int start = testEvidence.indexOf('`', index);
            if (start < 0) {
                break;
            }
            int end = testEvidence.indexOf('`', start + 1);
            if (end < 0) {
                break;
            }
            String candidate = testEvidence.substring(start + 1, end).trim();
            if (candidate.endsWith(".java")) {
                paths.add(candidate);
            }
            index = end + 1;
        }
        return paths;
    }

    record TestMatrixRow(String requirement, String testEvidence, String status) {}
}
