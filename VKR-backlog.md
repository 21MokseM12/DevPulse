# VKR Backlog (Prioritized)

Операционный backlog для приведения проекта к целевому решению из `VKR.pdf`.
Документ сфокусирован на практическом исполнении: приоритет, результат, зона изменений и зависимости.

## Правила приоритизации

- `P0` - критичный путь: без выполнения нельзя получить рабочее целевое решение.
- `P1` - высокий приоритет: функциональная и архитектурная полнота, качество и безопасность.
- `P2` - стабилизация и выпуск: инфраструктура, тестовая зрелость, финальная приемка.

---

## P0 (Critical Path)

### P0-1. Bot persistence (клиенты + уведомления)

- **Цель:** сделать `bot` самостоятельным сервисом хранения клиентов и уведомлений.
- **Definition of Done:**
  - в `bot` подключены DB-зависимости и миграции;
  - создана схема хранения клиентов и уведомлений;
  - `POST/DELETE /api/v1/clients` работают через БД `bot`;
  - входящие `LinkUpdate` сохраняются как уведомления.
- **Зона кода:**
  - `bot/pom.xml`
  - `bot/src/main/resources/application.yaml`
  - `bot/src/main/resources/db/**`
  - `bot/src/main/java/backend/academy/bot/**` (service/repository/controller)
- **Зависимости:** нет (стартовая задача критичного пути).

### P0-2. Kafka intake в Bot + единый pipeline обработки уведомлений

- **Цель:** обеспечить прием `LinkUpdate` не только по HTTP, но и по Kafka.
- **Definition of Done:**
  - в `bot` подключен Kafka consumer;
  - сообщения `LinkUpdate` из Kafka обрабатываются тем же сервисом, что и HTTP `/updates`;
  - поведение хранения уведомлений одинаковое для HTTP и Kafka.
- **Зона кода:**
  - `bot/pom.xml`
  - `bot/src/main/resources/application.yaml`
  - `bot/src/main/java/backend/academy/bot/config/**`
  - `bot/src/main/java/backend/academy/bot/**` (listener/service/controller)
- **Зависимости:** `P0-1`.

### P0-3. Outbox runtime в Scrapper (publisher + mark sent)

- **Цель:** довести Transactional Outbox до рабочего режима доставки.
- **Definition of Done:**
  - фоновый процесс читает `kafka_outbox` по `sent = false`;
  - сообщения публикуются в Kafka;
  - после успеха обновляются `sent`, `sent_at`, `attempt_count`;
  - topic берется из конфигурации, без hardcode.
- **Зона кода:**
  - `scrapper/src/main/java/backend/academy/scrapper/service/**`
  - `scrapper/src/main/java/backend/academy/scrapper/db/repository/**`
  - `scrapper/src/main/resources/application.yaml`
  - `scrapper/src/main/resources/db/query/kafka_outbox/**`
- **Зависимости:** желательно после `P0-2`, чтобы была готова целевая Kafka-цепочка.

---

## P1 (High Priority)

### P1-1. Безопасность: BCrypt и единый auth flow

- **Цель:** закрыть требования по хранению паролей и аутентификации.
- **Definition of Done:**
  - plain-text пароли исключены в `bot` и `scrapper`;
  - пароли хранятся в hash-виде (BCrypt);
  - проверка аутентификации согласована для клиентских операций.
- **Зона кода:**
  - `bot/src/main/java/backend/academy/bot/**`
  - `scrapper/src/main/java/backend/academy/scrapper/**`
  - миграции обоих сервисов (`*/src/main/resources/db/**`)
- **Зависимости:** `P0-1` (наличие БД в `bot`).

### P1-2. Polling-state и runtime-логика опроса

- **Цель:** синхронизировать фактический polling-процесс с целевой моделью состояния.
- **Definition of Done:**
  - `last_checked_at` корректно обновляется в каждом цикле;
  - `poll_state` используется в runtime (retry/backoff/last-success);
  - scheduler выбирает ссылки с учетом состояния опроса.
- **Зона кода:**
  - `scrapper/src/main/java/backend/academy/scrapper/service/listeners/LinkUpdateScheduledListener.java`
  - `scrapper/src/main/java/backend/academy/scrapper/service/impl/**`
  - `scrapper/src/main/java/backend/academy/scrapper/db/**`
  - `scrapper/src/main/resources/db/query/links/**`
- **Зависимости:** `P0-3`.

### P1-3. Фильтрация получателей по client filters

- **Цель:** формировать `clientsIds` только из релевантных подписчиков.
- **Definition of Done:**
  - при обнаружении события применяются фильтры подписки (`author`, `label` и т.п.);
  - нерелевантные клиенты не попадают в `clientsIds`;
  - поведение покрыто тестами на разные типы событий.
- **Зона кода:**
  - `scrapper/src/main/java/backend/academy/scrapper/service/updaters/**`
  - `scrapper/src/main/java/backend/academy/scrapper/service/impl/**`
  - `scrapper/src/main/java/backend/academy/scrapper/db/repository/**`
- **Зависимости:** `P1-2`.

### P1-4. Контрактная консистентность OpenAPI/AsyncAPI и runtime

- **Цель:** устранить расхождения между контрактами и фактическим поведением API/топиков.
- **Definition of Done:**
  - OpenAPI и контроллеры возвращают согласованные коды и DTO;
  - AsyncAPI соответствует фактическим топикам и payload;
  - обработка ошибок 400/404 единообразна в `bot` и `scrapper`.
- **Зона кода:**
  - `bot/contracts/openapi.yaml`
  - `scrapper/contracts/openapi.yaml`
  - `scrapper/contracts/asyncapi.yaml`
  - контроллеры/handler-ы в `bot` и `scrapper`.
- **Зависимости:** `P0-2`, `P0-3`.

---

## P2 (Stabilization & Release)

### P2-1. Инфраструктура локального окружения под целевую топологию

- **Цель:** привести локальный запуск к архитектуре ВКР.
- **Definition of Done:**
  - `docker-compose` поднимает `bot`, `scrapper`, две БД и Kafka-окружение;
  - env-переменные согласованы между сервисами;
  - убраны заведомо опасные fallback-конфиги.
- **Зона кода:**
  - `docker-compose.yaml`
  - `bot/src/main/resources/application.yaml`
  - `scrapper/src/main/resources/application.yaml`
  - `scrapper/src/main/java/backend/academy/scrapper/config/ClientConfig.java`
- **Зависимости:** `P0-2`, `P0-3`.

### P2-2. Integration/E2E тесты по критичному контуру

- **Цель:** зафиксировать поведение и защититься от регрессий.
- **Definition of Done:**
  - отключенные тесты в `bot` переактивированы или заменены эквивалентом;
  - есть сценарии HTTP/Kafka доставки, outbox retry, 400/404, subscribe/unsubscribe;
  - CI подтверждает прохождение интеграционных сценариев.
- **Зона кода:**
  - `bot/src/test/**`
  - `scrapper/src/test/**`
  - test config/resources в обоих модулях
- **Зависимости:** `P0-*`, `P1-*`.

### P2-3. Финальная приемка по DoD ВКР

- **Цель:** зафиксировать завершенность проекта перед защитой.
- **Definition of Done:**
  - выполнены все пункты общего DoD из `VKR-plan.md`;
  - подготовлен финальный чек-лист демонстрации сценариев;
  - подтверждена соответствие функциональным и ключевым нефункциональным требованиям.
- **Зона кода и артефактов:**
  - `VKR-plan.md`
  - `README.md`
  - контракты, тесты, compose-конфигурация
- **Зависимости:** `P2-1`, `P2-2`.

---

## Roadmap по итерациям

### Sprint 1

- `P0-1` Bot persistence
- `P0-2` Kafka intake в Bot

### Sprint 2

- `P0-3` Outbox runtime в Scrapper
- `P1-4` Контрактная консистентность (в части Kafka/HTTP)

### Sprint 3

- `P1-1` Безопасность (BCrypt + auth flow)
- `P1-2` Polling-state runtime
- `P1-3` Фильтрация получателей

### Sprint 4

- `P2-1` Инфраструктура окружения
- `P2-2` Integration/E2E тесты
- `P2-3` Финальная приемка

---

## Риски

- Высокая связность изменений между `bot` и `scrapper` при миграции на Kafka-путь.
- Риск контрактных регрессий при выравнивании OpenAPI/AsyncAPI и runtime.
- Рост сложности тестового окружения после добавления второй БД и Kafka в compose.
- Потенциальные проблемы миграции данных при переходе на hash-пароли.

## Quick Wins

- Убрать hardcoded topic из Scrapper и перейти на конфиг.
- Исправить опасный fallback `BASE_BOT_URL` в `ClientConfig`.
- Актуализировать `README.md` под текущий статус HTTP/Kafka и структуру окружения.
- Зафиксировать единый шаблон `ApiErrorResponse` для 400/404 в обоих сервисах.
