# Backend Test Matrix (F1-F15)

Матрица трассировки backend-требований VKR к контрактам и автотестам.

## Область

- Источник требований: `VKR.pdf` (backend-подмножество `F1..F15`).
- Android UI-only сценарии помечаются как `gap` и выносятся в отдельный backlog.
- Матрица используется как acceptance-gate перед релизом и перед защитой.

## Matrix

| Requirement | Backend scope | Contract evidence | Test evidence (contract/integration/e2e) | Status |
| --- | --- | --- | --- | --- |
| F1 | Регистрация чата для подписок | `bot/contracts/openapi.yaml`, `scrapper/contracts/openapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/controller/ClientControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/db/repository/ChatRepositoryTest.java` | green |
| F2 | Добавление ссылки на отслеживание | `scrapper/contracts/openapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/controller/LinkControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/service/LinkOperationProcessorTest.java` | green |
| F3 | Удаление подписки на ссылку | `scrapper/contracts/openapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/controller/LinkControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/db/repository/LinkToChatRepositoryTest.java` | green |
| F4 | Получение списка отслеживаемых ссылок | `scrapper/contracts/openapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/controller/LinkControllerTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/db/repository/LinkRepositoryTest.java` | green |
| F5 | Фильтрация/теги подписок и пользовательские настройки выдачи | `scrapper/contracts/openapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/service/filters/SubscriptionFilterServiceTest.java`<br>`bot/src/test/java/backend/academy/bot/utils/LinkSettingsParserTest.java` | green |
| F6 | Обработка обновлений GitHub/StackOverflow в scheduler pipeline | `scrapper/contracts/asyncapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/service/updaters/impl/GithubUpdaterServiceTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/service/updaters/impl/StackOverflowUpdaterServiceTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/service/listeners/LinkUpdateScheduledListenerTest.java` | green |
| F7 | Формирование уведомлений и intake в bot | `bot/contracts/openapi.yaml` | `bot/src/test/java/backend/academy/bot/integration/BotHttpLinkFlowIntegrationTest.java`<br>`bot/src/test/java/backend/academy/bot/controller/ScrapperControllerTest.java`<br>`bot/src/test/java/backend/academy/bot/service/notifications/BotNotificationManagerTest.java` | green |
| F8 | Доставка обновлений в режиме `http` | `scrapper/contracts/openapi.yaml`, `bot/contracts/openapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/service/notifications/impl/ScrapperHttpNotificationManagerTest.java`<br>`bot/src/test/java/backend/academy/bot/integration/BotHttpLinkFlowIntegrationTest.java` | green |
| F9 | Доставка обновлений в режиме `kafka` + outbox/retry/recovery | `scrapper/contracts/asyncapi.yaml`, `bot/contracts/asyncapi.yaml` | `scrapper/src/test/java/backend/academy/scrapper/integration_test/outbox/KafkaOutboxProcessorIntegrationTest.java`<br>`bot/src/test/java/backend/academy/bot/integration/BotKafkaNotificationPipelineIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/integration_test/kafka/KafkaLinkProcessTest.java` | green |
| F10 | Защита внутреннего `/updates` (authn/authz error paths) | `bot/contracts/openapi.yaml` | `bot/src/test/java/backend/academy/bot/integration/InternalUpdatesAuthIntegrationTest.java`<br>`bot/src/test/java/backend/academy/bot/config/SecurityConfigTest.java` | green |
| F11 | Persistence и миграции для backend-сервисов | `migrations/**`, `bot/contracts/openapi.yaml`, `scrapper/contracts/openapi.yaml` | `bot/src/test/java/backend/academy/bot/integration/BotPersistenceIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/db/impl/DbCommonServiceTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/db/impl/DbLinkServiceTest.java` | green |
| F12 | Мобильный UX уведомлений (экран/навигация/read-state UI) | Android repo + mobile contract | backend test evidence отсутствует в текущем репозитории | gap |
| F13 | Устойчивость внешних API (retry/backoff/circuit-breaker) | `scrapper/src/main/resources/application.yaml` | `scrapper/src/test/java/backend/academy/scrapper/service/resilience/ExternalApiResilienceExecutorTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/service/validators/impl/GithubAPILInkValidatorTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/service/validators/impl/StackOverflowAPILInkValidatorTest.java` | green |
| F14 | Инфраструктурная приемка (k8s manifests, CI/CD wiring) | `k8s/staging/*`, `k8s/production/*`, `.github/workflows/*.yaml` | `scrapper/src/test/java/backend/academy/scrapper/k8s/KubernetesManifestWiringIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/ci/GithubCdWorkflowIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/ci/GithubWorkflowStructureTest.java` | green |
| F15 | Эксплуатационная готовность (документация/runbooks/config) | `README.md`, `.env.example`, `docs/runbooks/*` | `scrapper/src/test/java/backend/academy/scrapper/docs/DocumentationConsistencyIntegrationTest.java`<br>`scrapper/src/test/java/backend/academy/scrapper/docs/RunbooksPackageIntegrationTest.java` | green |

## Gaps and backlog

- `F12`: требуется отдельная связка с Android-проектом и e2e-проверки клиентского UX.
- План закрытия gap: `BK-402` (финальная трассировка `VKR -> code -> test`) + Android acceptance в отдельном контуре.

## CI consistency check

- Lightweight check выполняется тестом `BackendTestMatrixIntegrationTest`:
  - проверяет полноту строк `F1..F15`,
  - валидирует статусы (`green|gap`),
  - проверяет существование файлов test evidence из матрицы.
