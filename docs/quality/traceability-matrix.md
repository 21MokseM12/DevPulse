# Backend Traceability Matrix (VKR -> code -> test)

Финальная карта трассировки для backend-части VKR: от требований к реализации и тестовому evidence.

## Статусы

- `implemented` — реализация присутствует в репозитории.
- `tested` — есть автоматическое подтверждение (contract/integration/e2e-like backend).
- `residual-risk` — есть ограничение или внешний dependency.

## Traceability

| Requirement | Implementation (modules/files) | Test evidence | Status | Residual risk |
| --- | --- | --- | --- | --- |
| F1 | `scrapper/src/main/java/backend/academy/scrapper/controller/ClientController.java`<br>`scrapper/src/main/java/backend/academy/scrapper/service/impl/ChatOperationProcessorImpl.java` | `scrapper/src/test/java/backend/academy/scrapper/controller/ClientControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/db/repository/ChatRepositoryTest.java` | implemented,tested | none |
| F2 | `scrapper/src/main/java/backend/academy/scrapper/controller/LinkController.java`<br>`scrapper/src/main/java/backend/academy/scrapper/service/impl/LinkOperationProcessorImpl.java` | `scrapper/src/test/java/backend/academy/scrapper/controller/LinkControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/service/LinkOperationProcessorTest.java` | implemented,tested | none |
| F3 | `scrapper/src/main/java/backend/academy/scrapper/controller/LinkController.java`<br>`scrapper/src/main/java/backend/academy/scrapper/db/repository/impl/LinkToChatRepositoryImpl.java` | `scrapper/src/test/java/backend/academy/scrapper/controller/LinkControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/db/repository/LinkToChatRepositoryTest.java` | implemented,tested | none |
| F4 | `scrapper/src/main/java/backend/academy/scrapper/controller/LinkController.java`<br>`scrapper/src/main/java/backend/academy/scrapper/db/repository/impl/LinkRepositoryImpl.java` | `scrapper/src/test/java/backend/academy/scrapper/controller/LinkControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/db/repository/LinkRepositoryTest.java` | implemented,tested | none |
| F5 | `scrapper/src/main/java/backend/academy/scrapper/service/filters/SubscriptionFilterService.java`<br>`bot/src/main/java/backend/academy/bot/utils/LinkSettingsParser.java` | `scrapper/src/test/java/backend/academy/scrapper/service/filters/SubscriptionFilterServiceTest.java`<br>`bot/src/test/java/backend/academy/bot/utils/LinkSettingsParserTest.java` | implemented,tested | none |
| F6 | `scrapper/src/main/java/backend/academy/scrapper/service/updaters/impl/GithubUpdaterService.java`<br>`scrapper/src/main/java/backend/academy/scrapper/service/updaters/impl/StackOverflowUpdaterService.java` | `scrapper/src/test/java/backend/academy/scrapper/service/updaters/impl/GithubUpdaterServiceTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/service/updaters/impl/StackOverflowUpdaterServiceTest.java` | implemented,tested | none |
| F7 | `bot/src/main/java/backend/academy/bot/controller/ScrapperController.java`<br>`bot/src/main/java/backend/academy/bot/service/notifications/BotNotificationManager.java` | `bot/src/test/java/backend/academy/bot/controller/ScrapperControllerTest.java`<br>`bot/src/test/java/backend/academy/bot/integration/BotHttpLinkFlowIntegrationTest.java` | implemented,tested | none |
| F8 | `scrapper/src/main/java/backend/academy/scrapper/service/notifications/impl/ScrapperHttpNotificationManager.java`<br>`bot/src/main/java/backend/academy/bot/service/ScrapperConnectionService.java` | `scrapper/src/test/java/backend/academy/scrapper/service/notifications/impl/ScrapperHttpNotificationManagerTest.java`<br>`bot/src/test/java/backend/academy/bot/integration/BotHttpLinkFlowIntegrationTest.java` | implemented,tested | none |
| F9 | `scrapper/src/main/java/backend/academy/scrapper/service/notifications/impl/KafkaOutboxProcessor.java`<br>`bot/src/main/java/backend/academy/bot/kafka/listener/LinkUpdateKafkaListener.java` | `scrapper/src/test/java/backend/academy/scrapper/integration_test/outbox/KafkaOutboxProcessorIntegrationTest.java`<br>`bot/src/test/java/backend/academy/bot/integration/BotKafkaNotificationPipelineIntegrationTest.java` | implemented,tested | none |
| F10 | `bot/src/main/java/backend/academy/bot/config/SecurityConfig.java`<br>`bot/src/main/java/backend/academy/bot/controller/ScrapperController.java` | `bot/src/test/java/backend/academy/bot/integration/InternalUpdatesAuthIntegrationTest.java`<br>`bot/src/test/java/backend/academy/bot/config/SecurityConfigTest.java` | implemented,tested | none |
| F11 | `bot/src/main/resources/db/migrations/`<br>`scrapper/src/main/resources/db/migrations/` | `bot/src/test/java/backend/academy/bot/integration/BotPersistenceIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/db/impl/DbCommonServiceTest.java` | implemented,tested | none |
| F12 | Android клиентский UX (внешний репозиторий) | backend-only репозиторий не содержит UI-тестов | implemented,residual-risk | требуется подтверждение в Android acceptance контуре |
| F13 | `scrapper/src/main/java/backend/academy/scrapper/service/resilience/ExternalApiResilienceExecutor.java`<br>`scrapper/src/main/resources/application.yaml` | `scrapper/src/test/java/backend/academy/scrapper/service/resilience/ExternalApiResilienceExecutorTest.java` | implemented,tested | none |
| F14 | `.github/workflows/build.yaml`<br>`.github/workflows/cd-staging.yaml`<br>`.github/workflows/cd-production.yaml`<br>`k8s/staging/*`, `k8s/production/*` | `scrapper/src/test/java/backend/academy/scrapper/ci/GithubCdWorkflowIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/k8s/KubernetesManifestWiringIntegrationTest.java` | implemented,tested | none |
| F15 | `README.md`<br>`.env.example`<br>`docs/runbooks/*` | `scrapper/src/test/java/backend/academy/scrapper/docs/DocumentationConsistencyIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/docs/RunbooksPackageIntegrationTest.java` | implemented,tested | none |

## Cross-links

- Backend test matrix: `docs/quality/backend-test-matrix.md` (`BK-401`).
- Acceptance checklist: `docs/quality/defense-acceptance-checklist.md`.

## Review status

- Версия матрицы: `2026-05-08`.
- Статус: готово к использованию в защите backend-части VKR.
