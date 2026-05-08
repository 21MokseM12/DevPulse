package backend.academy.scrapper.docs;

import java.util.List;

final class RunbookChecklistValidator {

    private static final List<String> REQUIRED_PROCEDURE_SECTIONS =
            List.of("### Preconditions", "### Expected output", "### Timeout", "### Failure actions");

    private RunbookChecklistValidator() {}

    static boolean hasRequiredProcedureSections(String content) {
        return REQUIRED_PROCEDURE_SECTIONS.stream().allMatch(content::contains);
    }
}
