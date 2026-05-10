package backend.academy.scrapper.docs;

final class ReadmeChecklistValidator {

    private ReadmeChecklistValidator() {}

    static boolean hasRequiredRuntimeSections(String readme) {
        return readme.contains("## Runtime и архитектура")
                && readme.contains("## Быстрый старт (clone -> configure -> run -> verify)")
                && readme.contains("## Конфигурация окружений");
    }

    static boolean hasDeliveryModeDetails(String readme) {
        return readme.contains("## Режимы доставки `http|kafka`")
                && readme.contains("SCRAPPER_DELIVERY_MODE")
                && readme.contains("### Что нужно задать в окружении")
                && readme.contains("### Поведение и ограничения");
    }

    static boolean hasResilienceGuide(String readme) {
        return readme.contains("## Resilience: timeout/retry/backoff/circuit breaker")
                && readme.contains("### Runtime-политики в backend")
                && readme.contains("### Рекомендуемые значения для production")
                && readme.contains("### Как проверить, что политика активна");
    }
}
