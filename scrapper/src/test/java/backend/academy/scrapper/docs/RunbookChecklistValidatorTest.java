package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RunbookChecklistValidatorTest {

    @Test
    void hasRequiredProcedureSections_shouldReturnTrueWhenAllSectionsPresent() {
        String runbook =
                """
            ### Preconditions
            test
            ### Expected output
            test
            ### Timeout
            test
            ### Failure actions
            test
            """;

        assertTrue(RunbookChecklistValidator.hasRequiredProcedureSections(runbook));
    }

    @Test
    void hasRequiredProcedureSections_shouldReturnFalseWhenAnySectionMissing() {
        String runbook =
                """
            ### Preconditions
            test
            ### Expected output
            test
            ### Timeout
            test
            """;

        assertFalse(RunbookChecklistValidator.hasRequiredProcedureSections(runbook));
    }
}
