package backend.academy.scrapper.docs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReadmeChecklistValidatorTest {

    @Test
    void hasRequiredRuntimeSections_shouldReturnTrueWhenSectionsPresent() {
        String readme =
                """
            ## Runtime и архитектура
            ## Быстрый старт (clone -> configure -> run -> verify)
            ## Конфигурация окружений
            """;

        assertTrue(ReadmeChecklistValidator.hasRequiredRuntimeSections(readme));
    }

    @Test
    void hasDeliveryModeDetails_shouldReturnFalseWhenDeliverySectionIncomplete() {
        String readme =
                """
            ## Режимы доставки `http|kafka`
            SCRAPPER_DELIVERY_MODE
            """;

        assertFalse(ReadmeChecklistValidator.hasDeliveryModeDetails(readme));
    }

    @Test
    void hasResilienceGuide_shouldReturnTrueWhenAllRequiredBlocksPresent() {
        String readme =
                """
            ## Resilience: timeout/retry/backoff/circuit breaker
            ### Runtime-политики в backend
            ### Рекомендуемые значения для production
            ### Как проверить, что политика активна
            """;

        assertTrue(ReadmeChecklistValidator.hasResilienceGuide(readme));
    }
}
