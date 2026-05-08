# VKR Backend Backlog (100% Target, Detailed)

Backend-only backlog для доведения репозитория до полного соответствия `VKR.pdf`.
Android-часть не учитывается (отдельный проект).

## Статусы

- `done` — уже реализовано в backend
- `todo` — требуется реализация
- `blocked` — есть внешний блокер

---

## 0. Уже закрыто в backend

### BK-000. Микросервисный контур Bot + Scrapper

- **Статус:** `done`
- **Факт:** два сервиса, отдельные БД, контрактная связка между сервисами.

### BK-001. F1-F7 (клиенты/подписки/tags/filters)

- **Статус:** `done`
- **Факт:** регистрация/удаление клиента, add/remove/list links, хранение tags/filters.

### BK-002. F10/F11 (доставка) + outbox основа

- **Статус:** `done`
- **Факт:** HTTP и Kafka intake в `bot`, outbox processor в `scrapper` присутствует.

### BK-003. F13-F15 (валидация/ошибки/логирование)

- **Статус:** `done`
- **Факт:** единая ошибка, валидация входа, базовое логирование.

### BK-004. Безопасность хранения паролей

- **Статус:** `done`
- **Факт:** хеширование паролей и миграции уже есть.

---

## 1. Критические backend-gap’ы до 100% VKR

### BK-101. GitHub conditional polling (`ETag` + `If-None-Match`)

- **Статус:** `done`
- **Почему:** VKR требует экономию лимитов GitHub через условные запросы.
- **API-контекст:**
  - GitHub endpoint: `GET /repos/{owner}/{repo}/events`
  - заголовки: `ETag`, `If-None-Match`
  - expected branch: `200` (события) / `304` (без изменений)
- **Декомпозиция:**
  1. Расширить HTTP-клиент GitHub поддержкой заголовка `If-None-Match`.
  2. Считывать `ETag` из ответа `200` и сохранять в `links.etag`.
  3. При `304` обновлять состояние опроса без обработки payload.
  4. Обновить scheduler flow, чтобы `etag` реально использовался каждый цикл.
- **Проверки:**
  - integration test: первый `200` -> сохраняется `etag`.
  - integration test: повторный опрос с тем же `etag` -> `304`, без новых уведомлений.
- **DoD:** `etag` перестает быть “мертвым полем”, polling экономит API лимиты.
- **Зависимости:** нет.

### BK-102. StackOverflow poll-state как источник истины

- **Статус:** `todo`
- **Почему:** VKR требует устойчивый polling без дублей и пропусков.
- **API-контекст:**
  - StackExchange endpoints: `GET /questions/{id}`, `GET /questions/{id}/answers`, `GET /questions/{id}/comments`
  - фильтрация по времени/состоянию (`fromdate`, `last_event_date`, `next_retry_at`)
- **Декомпозиция:**
  1. Формализовать чтение `poll_state` перед каждым опросом.
  2. Обновлять `last_checked_at`, `last_event_date`, `fail_count`, `next_retry_at` по единым правилам.
  3. Гарантировать дедупликацию событий при пагинации/повторном запуске.
  4. Уточнить SQL выборки и update-запросы для backoff.
- **Проверки:**
  - integration test: ошибка API увеличивает `fail_count` и сдвигает `next_retry_at`.
  - integration test: успешный цикл сбрасывает backoff и не дублирует события.
- **DoD:** scheduler принимает решения из `poll_state`, поведение воспроизводимо.
- **Зависимости:** нет.

### BK-103. Resilience4j для внешних API (GitHub/StackOverflow/Bot)

- **Статус:** `todo`
- **Почему:** в `VKR.pdf` для интеграции с внешними API зафиксированы `CircuitBreaker`, `Retry` и `RateLimiter`, чтобы polling-контур не деградировал каскадно при проблемах GitHub/StackOverflow/Bot.
- **As-is (текущий код):**
  - `scrapper` использует `RestClient`/`HttpServiceProxyFactory` без полного контура resilience для `GithubClient`, `StackOverflowClient`, `BotClient`.
  - В конфигурации `scrapper/src/main/resources/application.yaml` нет целостного policy-блока с параметрами breaker/retry/limiter под внешние REST-вызовы.
  - Отдельные retry-политики есть в Kafka-пути, но они не закрывают HTTP-интеграции BK-103.
- **To-be (целевой контур):**
  - Для трех внешних направлений (`GitHub`, `StackOverflow`, `Bot /updates`) введены именованные resilience policy.
  - Retry применяется только к временным сбоям (`5xx`, timeout, transport errors, rate-limit окна), не ретраит логические `4xx`.
  - Breaker предотвращает каскадные сбои scheduler-потока, limiter контролирует частоту вызовов без блокировки всей системы.
- **Декомпозиция реализации:**
  1. Добавить и выровнять зависимости resilience в `pom.xml`/модулях (`scrapper`, при необходимости `bot` для внешних HTTP-клиентов).
  2. Описать policy в `application.yaml`: отдельные инстансы для `githubApi`, `stackOverflowApi`, `botApi`.
  3. Ввести timeout-параметры HTTP-клиентов и связать их с retry policy.
  4. Обернуть исходящие вызовы `GithubClient` в `retry + circuit breaker + rate limiter`.
  5. Аналогично обернуть `StackOverflowClient` (включая paths `question/answers/comments`).
  6. Обернуть `BotClient.sendUpdates` для управляемого поведения при деградации `bot`.
  7. Явно определить список retriable/non-retriable исключений и HTTP-кодов.
  8. Согласовать fallback: ошибка должна корректно отражаться в `poll_state` (рост `fail_count`, backoff через `next_retry_at`), а не теряться.
  9. Добавить структурированное логирование инцидентов (retry exhausted, breaker open, limiter denied) с `linkId/url/client`.
  10. Экспортировать метрики resilience в actuator/monitoring контур.
- **Тест-план:**
  - unit: breaker переходит `CLOSED -> OPEN -> HALF_OPEN -> CLOSED` по настраиваемым порогам.
  - unit: retry срабатывает только на retriable-кейсы; `400/401/403/404` проходят без повторов.
  - integration (WireMock/Testcontainers): серия `5xx` открывает breaker и сокращает сетевые попытки.
  - integration: limiter удерживает частоту вызовов при массовом polling без starvation scheduler.
  - integration: при исчерпании retry корректно обновляется `poll_state` (`fail_count`, `next_retry_at`).
- **Definition of Done:**
  - Внешние REST-вызовы выполняются через policy resilience, а не «сырой» клиент.
  - Поведение при деградации воспроизводимо из конфигурации и подтверждено автотестами.
  - Наблюдаемость достаточна для операционной диагностики (логи + метрики).
- **Риски и зависимости:**
  - Риск неверной настройки limiter (лишняя задержка polling).
  - Нужна синхронизация с BK-102 (poll_state/backoff), чтобы fallback-логика не конфликтовала.
- **Зависимости:** нет.

### BK-104. Строгий режим доставки `http|kafka` (без двусмысленности)

- **Статус:** `todo`
- **Почему:** в `VKR.pdf` доставка уведомлений описана как переключаемый режим (`http` **или** `kafka`), выбираемый конфигурацией без смешанного поведения.
- **As-is (текущий код):**
  - В `scrapper` уведомление проходит гибридно: HTTP-отправка и запись в outbox/Kafka могут участвовать одновременно.
  - В `bot` обычно активны оба intake-канала (`POST /updates` и Kafka listener), что создает риск двойного приема одной бизнес-событийности.
  - Часть дублей гасится дедупликацией в БД, но это не заменяет строгий one-mode контракт.
- **To-be (целевой контур):**
  - Один конфиг (`delivery.mode=http|kafka`) определяет единственный путь публикации в `scrapper`.
  - Для `http` режима outbox/Kafka-путь не участвует в отправке текущего события.
  - Для `kafka` режима HTTP-путь не участвует, а доставка идет через outbox-процессор.
- **Декомпозиция реализации:**
  1. Ввести/зафиксировать единый `delivery.mode` в конфигурации и валидации startup.
  2. Выделить `NotificationSender`/`UpdateSender` интерфейс с двумя отдельными реализациями.
  3. Подключить реализации через `@ConditionalOnProperty` (или эквивалентную фабрику).
  4. Убрать смешанную отправку из одного сервиса/метода.
  5. Для `kafka` режима зафиксировать обязательный путь через `kafka_outbox` + `KafkaOutboxProcessor`.
  6. В `bot` задокументировать совместимый intake-профиль и fail-fast проверку несовместимой конфигурации.
  7. Обновить `application.yaml`, `.env.example`, `docker-compose` и runtime docs по режимам.
  8. Уточнить идемпотентность как safety-net, а не как механизм «штатного гибридного режима».
- **Тест-план:**
  - integration (`http`): событие уходит только через `BotClient`, в outbox не появляется новая запись отправки.
  - integration (`kafka`): событие записывается в outbox и публикуется в Kafka, HTTP вызовов нет.
  - integration (`bot`): при выбранном режиме обработки активен ожидаемый intake path.
  - regression: scheduler/update detector не зависит от конкретного транспорта доставки.
- **Definition of Done:**
  - Режим доставки детерминирован конфигом и не допускает двойной отправки по умолчанию.
  - Поведение `http` и `kafka` подтверждено отдельными интеграционными тестами.
  - Документация запуска описывает режимы явно и без двусмысленности.
- **Риски и зависимости:**
  - Риск нарушения обратной совместимости тестов, завязанных на гибрид.
  - Зависит от BK-106 по финальной модели идемпотентности intake.
- **Зависимости:** нет.

### BK-105. Межсервисная защита `scrapper -> bot` endpoint `/updates`

- **Статус:** `todo`
- **Почему:** VKR описывает изолированный внутренний контур взаимодействия сервисов; endpoint `/updates` не должен быть доступен неавторизованному трафику.
- **As-is (текущий код):**
  - `bot` endpoint `/updates` принимает запросы без обязательной межсервисной проверки секрета.
  - `scrapper` `BotClient` не гарантирует передачу обязательного internal auth header в этом направлении.
  - В тестах покрыт в основном позитивный сценарий, негативные проверки на секрет отсутствуют.
- **To-be (целевой контур):**
  - `POST /updates` в `bot` защищен обязательным внутренним заголовком (shared secret).
  - `scrapper` всегда отправляет этот заголовок в `BotClient`.
  - Без секрета/с неверным секретом запрос отвергается (`401`/`403` по принятой политике).
- **Декомпозиция реализации:**
  1. Зафиксировать контракт безопасности `/updates` (имя заголовка, тип ошибки, обязательность).
  2. Добавить в `bot` interceptor/filter для проверки internal header только на `/updates`.
  3. Подключить безопасное сравнение секрета и защиту от утечки значения в логах.
  4. В `scrapper` добавить `defaultHeader` для `BotClient` из конфигурации auth credentials.
  5. Вынести параметры секрета/заголовка в `application.yaml` и env-переменные обоих сервисов.
  6. Обновить `bot/contracts/openapi.yaml` (security requirements, expected `401/403`).
  7. Добавить migration notes/runbook по ротации секрета.
  8. Обновить интеграционные и WebMvc тесты под новый защищенный контракт.
- **Тест-план:**
  - WebMvc: `/updates` без заголовка -> `401/403`.
  - WebMvc: `/updates` с неверным секретом -> `401/403`.
  - WebMvc: `/updates` с валидным секретом -> `200`.
  - integration: `scrapper -> bot` успешен при согласованных env и падает при рассинхронизации секрета.
  - security regression: публичные `bot` API (`/api/v1/...`) не ломаются от internal проверки.
- **Definition of Done:**
  - `/updates` недоступен без валидной межсервисной авторизации.
  - `scrapper` и `bot` используют согласованный и конфигурируемый секрет.
  - Негативные и позитивные security-кейсы автоматизированы тестами.
- **Риски и зависимости:**
  - Риск временного недоставления уведомлений при рассинхронизации env между сервисами.
  - Нужна координация с deployment-пайплайном для атомарной ротации секрета.
- **Зависимости:** нет.

### BK-106. Notification API в `bot` (backend side)

- **Статус:** `todo`
- **Почему:** в `VKR.pdf` пользовательский контур уведомлений включает список уведомлений, индикатор непрочитанных и фильтрацию по тегам; backend `bot` должен предоставить полноценный API для этого сценария.
- **As-is (текущий код):**
  - В `bot/contracts/openapi.yaml` есть intake `POST /updates`, но нет отдельного API чтения/статуса уведомлений.
  - Репозиторный слой `notifications` ориентирован на прием и сохранение, а не на query/read lifecycle.
  - Семантика read/unread и API-операции `mark read` в текущем контракте не зафиксированы.
- **To-be (целевой контур):**
  - `bot` предоставляет endpoints: `list notifications`, `unread count`, `mark read` (+ batch/ids), `filter by tags`.
  - Состояние прочитанности хранится явно и атомарно обновляется.
  - Идемпотентность intake (HTTP/Kafka) не ломает пользовательский read/unread lifecycle.
- **Контрактный контекст:**
  - расширение `bot/contracts/openapi.yaml`;
  - новые endpoints: `list`, `unread count`, `mark read`, `filter by tags`.
- **Декомпозиция реализации:**
  1. Расширить OpenAPI контракт: схемы `NotificationDto`, list response, unread response, mark-read request/response.
  2. Определить модель хранения read/unread (`read_at` или `is_read` на связи получатель-уведомление) и подготовить миграцию.
  3. Реализовать репозиторные запросы: пагинация списка, подсчет unread, update read-status.
  4. Добавить сервисный слой с проверками принадлежности уведомления клиенту.
  5. Добавить REST controller для notification operations с заголовком `Client-Login`.
  6. Реализовать фильтрацию по тегам (через snapshot тегов или join с подписками по принятой модели).
  7. Зафиксировать политику идемпотентности входящих событий (`HTTP/Kafka`) и влияние на read-status.
  8. Обновить контрактные тесты, интеграционные тесты БД и e2e сценарии уведомлений.
  9. Добавить backward-compatible обработку для клиентов, которые еще не используют новые endpoints.
- **Тест-план:**
  - contract: схемы и коды ответов новых notification endpoints.
  - integration: lifecycle `new -> unread -> read`, включая повторный `mark read` (идемпотентность).
  - integration: фильтрация уведомлений по тегам возвращает корректный subset.
  - integration: unread count корректен после intake событий из HTTP и Kafka.
  - security/data isolation: клиент не видит и не помечает чужие уведомления.
- **Definition of Done:**
  - `bot` предоставляет API чтения и управления статусом уведомлений, достаточный для Android UI VKR.
  - read/unread состояние персистентно и корректно при повторных доставках.
  - Фильтрация по тегам и unread count подтверждены автотестами.
- **Риски и зависимости:**
  - Риск усложнения схемы БД и миграции существующих уведомлений.
  - Зависит от BK-104 (строгий transport mode) и BK-105 (защищенный intake), чтобы модель была согласованной end-to-end.
- **Зависимости:** нет.

---

## 2. Инфраструктура и развёртывание (VKR sections 2.5 / 3.5)

### BK-201. Redis в runtime и реальный usage

- **Статус:** `done`
- **Почему:** в `VKR.pdf` Redis зафиксирован как runtime-компонент для кэша ETag и метаданных polling; это снижает нагрузку на PostgreSQL и ускоряет цикл опроса.
- **As-is (текущий код):**
  - В `docker-compose.yaml` отсутствует сервис Redis (поднимаются `bot`, `scrapper`, две PostgreSQL и Kafka/Redpanda).
  - В `bot/pom.xml` и `scrapper/pom.xml` зависимость `spring-boot-starter-data-redis` закомментирована.
  - В `application.yaml` обоих сервисов нет `spring.data.redis.*` конфигурации.
  - ETag/polling state фактически ведутся через PostgreSQL (`links`, `poll_state`) и JDBC-репозитории, а не через Redis.
  - Есть тестовая заготовка Redis-контейнера в `bot` (`TestcontainersConfiguration`), но она отключена (`@Disabled`).
- **To-be (целевой контур):**
  - Redis работает как runtime-кэш для hot-path данных polling (минимум ETag и poll hints).
  - PostgreSQL остается source of truth, Redis используется как ускоряющий слой с явной стратегией TTL/инвалидации.
  - Локальный и тестовый контуры поднимают Redis воспроизводимо.
- **Декомпозиция реализации:**
  1. Добавить Redis-сервис в `docker-compose.yaml` (image, port, healthcheck, volume при необходимости).
  2. Добавить env-параметры Redis в runtime-конфиг (`host`, `port`, `password`, `database`) для обоих сервисов.
  3. Активировать `spring-boot-starter-data-redis` в `scrapper` (и в `bot`, если кэш нужен с двух сторон).
  4. Добавить `spring.data.redis.*` в `scrapper/src/main/resources/application.yaml` и test-profile.
  5. Определить кэшируемые ключи и формат (`etag:{linkId}`, `poll:lastCheck:{linkId}` и т.п.) + TTL policy.
  6. Реализовать Redis-адаптер/сервис для чтения/записи ETag с fallback в БД.
  7. Встроить кэш в polling flow: read-through перед API вызовом и write-through после успешного цикла.
  8. Зафиксировать стратегию инвалидации/синхронизации Redis при обновлении `poll_state`.
  9. Добавить метрики hit/miss/latency по Redis операциям.
  10. Подключить Redis в интеграционные тесты (Testcontainers) и снять отключенные заглушки.
- **Тест-план:**
  - integration: `docker compose up` поднимает Redis и сервисы стартуют с активным Redis connection.
  - integration: кэш ETag реально читается/обновляется в Redis и не расходится с БД.
  - integration: при недоступном Redis сервис переходит в безопасный fallback (без потери корректности polling).
  - testcontainers: smoke `SET/GET` и сценарий polling с cache hit/miss.
- **Definition of Done:**
  - Redis присутствует в runtime-контуре и используется в рабочем коде, а не только в зависимости/доках.
  - Есть автоматические тесты на Redis path и fallback path.
  - Наблюдаемость кэша (минимум hit/miss + ошибки) доступна в мониторинге.
- **Риски и зависимости:**
  - Риск рассинхронизации Redis и БД без явной инвалидации.
  - Зависит от BK-101/BK-102, чтобы кэш встраивался в финальный polling-state/ETag flow.
- **Зависимости:** `BK-101`, `BK-102` желательно.

### BK-202. Топология Kafka + Zookeeper (literal VKR match)

- **Статус:** `todo`
- **Почему:** `VKR.pdf` буквально фиксирует компонентный стек `Apache Kafka + Zookeeper`; для full-alignment локальная инфраструктура должна совпадать с этим описанием.
- **As-is (текущий код):**
  - В `docker-compose.yaml` сервис `kafka` реализован образом `redpandadata/redpanda`, отдельного `zookeeper` нет.
  - `bot`/`scrapper` используют bootstrap `kafka:9092`, что совместимо с Kafka API, но не соответствует literal топологии VKR.
  - Testcontainers используют разные Kafka-образы в разных модулях; единая инфраструктурная модель не зафиксирована.
  - Топики в локальном окружении в основном полагаются на auto-create.
- **To-be (целевой контур):**
  - В локальном контуре используются отдельные сервисы `kafka` и `zookeeper` (или строго согласованный аналог, если VKR обновлен).
  - Producer/consumer/outbox конфиги работают без изменений бизнес-логики.
  - Topic bootstrap и health/startup поведения явно контролируются.
- **Декомпозиция реализации:**
  1. Заменить Redpanda-контур в `docker-compose.yaml` на `kafka + zookeeper`.
  2. Настроить listeners/advertised listeners для внутренних и внешних подключений.
  3. Добавить healthcheck для `zookeeper` и `kafka`, перевести `depends_on` сервисов на `service_healthy`.
  4. Зафиксировать создание критичных топиков (`link-updates` и служебные) явным bootstrap шагом.
  5. Проверить/адаптировать `BOT_KAFKA_BOOTSTRAP_SERVERS` и `SCRAPPER_KAFKA_BOOTSTRAP_SERVERS` под новую топологию.
  6. Провалидировать producer settings (`acks`, idempotence, retries) в новом окружении.
  7. Провалидировать consumer retry/backoff/group settings и смещения.
  8. Проверить outbox pipeline `scrapper -> topic -> bot listener` на новой инфраструктуре.
  9. Выровнять testcontainers-подход с целевой runtime-моделью.
  10. Обновить README/.env/runtime инструкции под Kafka+Zookeeper стек.
- **Тест-план:**
  - integration: сквозной pipeline из outbox в `bot` работает в новом compose-контуре.
  - integration: сценарий рестарта брокера не ломает доставку после восстановления.
  - smoke: топики создаются и доступны до старта обработки.
  - regression: HTTP mode доставки не затрагивается миграцией брокера.
- **Definition of Done:**
  - Локальная инфраструктура буквально соответствует топологии VKR (`Kafka + Zookeeper`).
  - Сквозная Kafka-доставка подтверждена интеграционными тестами.
  - Документация запуска и env полностью соответствуют новой схеме.
- **Риски и зависимости:**
  - Риск флаки-старта при неправильной настройке listeners/depends_on.
  - Желательно синхронизировать с BK-104, чтобы transport mode тестировался на финальной топологии.
- **Зависимости:** нет.

### BK-203. Kubernetes manifests (`staging` и `production`)

- **Статус:** `todo`
- **Почему:** в `VKR.pdf` описан целевой k8s-контур (`staging/production`, probes, autoscaling, secrets/configmaps); без манифестов в репозитории этот контур не воспроизводим.
- **As-is (текущий код):**
  - В репозитории нет директории `k8s/` и нет манифестов `staging/production`.
  - Runtime сегодня ограничен `docker-compose.yaml`.
  - CI workflow не выполняет deploy-шаги в Kubernetes.
- **To-be (целевой контур):**
  - В репозитории присутствуют `k8s/staging` и `k8s/production` как source of truth.
  - Развертывание включает stateless (bot/scrapper) и stateful (postgres/kafka/redis по выбранной стратегии) компоненты.
  - Rolling update, probes и HPA зафиксированы и проверяемы.
- **Декомпозиция реализации:**
  1. Создать структуру `k8s/staging` и `k8s/production` с единым шаблоном именования.
  2. Добавить Deployments для `bot` и `scrapper` (replicas, resources, envFrom/config).
  3. Добавить Services: внешний доступ только для `bot`, внутренние сервисы для остальных.
  4. Добавить ConfigMaps для runtime-конфигов (`delivery mode`, polling interval, broker/db urls).
  5. Добавить Secrets для паролей/токенов/внутреннего секрета.
  6. Добавить liveness/readiness probes на actuator endpoints.
  7. Зафиксировать rolling update policy (`maxUnavailable/maxSurge`) и graceful shutdown параметры.
  8. Добавить HPA для `bot` и `scrapper` с базовыми CPU/Memory метриками.
  9. Добавить манифесты/стратегию stateful-компонентов (StatefulSet/PVC) или задокументировать внешние managed сервисы.
  10. Описать apply/rollback команды и порядок деплоя для staging/prod.
- **Тест-план:**
  - `kubectl apply --dry-run=server` для обоих окружений.
  - deploy в staging + проверка `readiness/liveness` и сетевой связности сервисов.
  - smoke после деплоя: регистрация клиента, добавление ссылки, доставка обновления.
  - rollback test: откат ревизии deployment при неуспешном health.
- **Definition of Done:**
  - Backend полностью разворачивается в k8s по артефактам текущего репозитория.
  - Для `staging` и `production` есть разделенные и валидные манифесты.
  - Probes/HPA/rolling policy подтверждены практическим прогоном.
- **Риски и зависимости:**
  - Риск выбора неверной стратегии stateful-компонентов (in-cluster vs managed).
  - Зависит от BK-201/BK-202, чтобы инфраструктурные зависимости были финализированы.
- **Зависимости:** `BK-201`, `BK-202` желательно.

### BK-204. Полный CI/CD pipeline по VKR

- **Статус:** `todo`
- **Почему:** `VKR.pdf` задает полный релизный путь (CI + CD + health + rollback); текущий репозиторий содержит только CI workflow без автоматизированного деплоя.
- **As-is (текущий код):**
  - В `.github/workflows` есть только `build.yaml`.
  - Текущий workflow покрывает `mvn verify`, линтинг/статический анализ и интеграционные тесты.
  - В pipeline нет docker build/push, staging/prod deploy, post-deploy health-check и rollback.
  - Ветки/окружения (`staging`, `production`) и k8s-манифесты пока не подключены к CI/CD.
- **To-be (целевой контур):**
  - CI отвечает за сборку/тесты/quality gates и формирует deployable artifact (образы).
  - CD автоматически разворачивает `staging`, затем по approval — `production`.
  - Health-check и rollback встроены в pipeline как обязательные шаги.
- **Декомпозиция реализации:**
  1. Разделить workflows на `ci`, `cd-staging`, `cd-production` (или эквивалентную схему jobs/environments).
  2. Зафиксировать триггеры веток/PR, соответствующие целевому release flow.
  3. Добавить docker build для `bot` и `scrapper` и push в registry с immutable tags.
  4. Добавить деплой в `staging` из pipeline (kubectl/helm) с использованием `k8s/staging`.
  5. Добавить post-deploy smoke/health-check в staging.
  6. Добавить gated promote в `production` через GitHub Environments/approval.
  7. Добавить деплой в production по `k8s/production`.
  8. Добавить автоматический rollback на провал health-check и публикацию причины ошибки.
  9. Добавить артефакты/логи для трассировки релиза (image tag -> commit -> environment).
  10. Синхронизировать README/runbook с реальным пайплайном.
- **Тест-план:**
  - CI: полный прогон `build/linter/integration` на PR без регрессий.
  - staging CD: успешный deploy и зеленые `actuator/health` + smoke сценарий.
  - production CD: успешный promote по approval.
  - negative test: искусственно проваленный health вызывает rollback.
- **Definition of Done:**
  - Путь `PR -> staging -> production` автоматизирован и воспроизводим в репозитории.
  - Docker образы собираются/публикуются pipeline-ом, а не вручную.
  - Rollback проверен практическим сценарием и документирован.
- **Риски и зависимости:**
  - Основной риск: запуск CD до готовности BK-203 (нет target manifests).
  - Зависит от BK-203 и runbook-работ BK-302 для эксплуатационной зрелости.
- **Зависимости:** `BK-203`.

---

## 3. Документация и эксплуатация

### BK-301. `.env.example` и актуальный README

- **Статус:** `done`
- **Почему:** по `VKR.pdf` развертывание должно опираться на воспроизводимую документацию (`.env` шаблон, команды запуска, health-проверки), без «устных» шагов.
- **As-is (текущий код):**
  - `README.md` существует и уже содержит разделы по локальному запуску, CI/CD и k8s.
  - В репозитории отсутствует `.env.example` (шаблон переменных окружения не оформлен отдельным артефактом).
  - README частично смешивает исторические и актуальные формулировки (например, нефункциональные пункты про «только HTTP» рядом с текущим `http|kafka` контуром).
  - Перечень env-переменных указан в тексте README, но не структурирован как валидируемый machine-friendly шаблон.
- **To-be (целевой контур):**
  - В корне репозитория есть поддерживаемый `.env.example` для всех обязательных backend переменных.
  - README синхронизирован с реальным состоянием кода (compose, k8s, CI/CD, delivery modes, security).
  - Документация дает единый путь «clone -> configure -> run -> verify».
- **Декомпозиция реализации:**
  1. Создать `.env.example` в корне с обязательными ключами для `bot`, `scrapper`, PostgreSQL, Kafka, Redis, internal secret и внешних API.
  2. Разделить в `.env.example` переменные по секциям (`Bot`, `Scrapper`, `Infra`, `Security`, `External APIs`).
  3. Добавить комментарии к переменным (назначение, дефолт, обязательность, пример формата).
  4. Обновить README: актуальный стек runtime и поддерживаемые режимы доставки (`http|kafka`).
  5. Явно описать локальный запуск через `docker compose` и отдельный путь для k8s (`staging`, `production`).
  6. Добавить блоки «быстрый старт» и «проверка здоровья» с командами (`actuator/health`, базовые smoke-сценарии).
  7. Добавить раздел «миграция конфигурации» (что меняется при переходе compose -> k8s secrets/configmaps).
  8. Проверить, что README не содержит устаревших утверждений, противоречащих текущему коду/пайплайнам.
  9. Добавить ссылку на runbook-пакет BK-302 и перечень эксплуатационных документов.
- **Тест-план:**
  - doc smoke: чистый разработчик поднимает backend только по README + `.env.example`.
  - config validation: сервисы стартуют с `.env` на базе шаблона без ручного добора скрытых параметров.
  - consistency check: переменные из `.env.example` покрывают все обязательные `application.yaml` placeholders.
- **Definition of Done:**
  - `.env.example` присутствует и достаточен для локального старта backend.
  - README синхронизирован с фактической архитектурой и release-процессом.
  - Документация проходит практический smoke-прогон без дополнительных устных инструкций.
- **Риски и зависимости:**
  - Риск устаревания документации при изменениях CI/CD и k8s-манифестов.
  - Зависит от BK-201/BK-202/BK-203/BK-204, чтобы финальные команды и переменные были стабильны.
- **Зависимости:** `BK-201`, `BK-202`.

### BK-302. Runbook-и (backup/restore/rollback/smoke)

- **Статус:** `done`
- **Почему:** `VKR.pdf` требует эксплуатационную готовность: резервное копирование, восстановление, откат и быстрые post-deploy проверки должны быть формализованы.
- **As-is (текущий код):**
  - В `k8s/staging/README.md` и `k8s/production/README.md` есть базовые команды деплоя/проверки и rollback для deployment.
  - Полного централизованного runbook-пакета в репозитории нет (backup/restore/smoke/incident playbook разрознен или отсутствует).
  - Нет отдельного документа с процедурами `pg_dump/restore` для обеих БД в runtime и k8s-контуре.
  - Нет единой матрицы «симптом -> первичная диагностика -> действие».
- **To-be (целевой контур):**
  - В репозитории есть набор runbook-документов для операций уровня SRE/DevOps.
  - Процедуры backup/restore/rollback/smoke формализованы пошагово с критериями успеха.
  - Есть quick incident guide для типовых отказов (DB, Kafka, external API, internal auth, k8s rollout).
- **Декомпозиция реализации:**
  1. Создать каталог runbook-документации (например, `docs/runbooks/`).
  2. Добавить `backup-restore-postgres.md` для `bot-db` и `scrapper-db` (compose и k8s варианты).
  3. Добавить `rollback-cd-k8s.md` с процедурами rollback в staging/prod и проверкой последствий.
  4. Добавить `post-deploy-smoke.md` с минимальным набором API/инфра проверок после релиза.
  5. Добавить `incident-quick-guide.md` с triage-порядком и командами первичной диагностики.
  6. Для каждой процедуры зафиксировать preconditions, expected output, timeout, failure actions.
  7. Добавить блоки про безопасную работу с секретами (где брать, как ротировать, чего не логировать).
  8. Добавить связь runbook-ов с CI/CD (`cd-staging`, `cd-production`) и k8s-манифестами.
  9. Прогнать tabletop-проверку: выполнить хотя бы один backup/restore и один rollback по инструкции.
- **Тест-план:**
  - restore drill: восстановление тестовой БД из свежего backup проходит по runbook шагам.
  - rollback drill: искусственно проваленный deploy откатывается по документированной процедуре.
  - smoke drill: post-deploy чеклист выявляет критичный отказ в контролируемом сценарии.
- **Definition of Done:**
  - В репозитории есть полный runbook-пакет для backup/restore/rollback/smoke/incidents.
  - Минимум по одному практическому прогону на каждую процедуру выполнен и зафиксирован.
  - Эксплуатационные действия воспроизводимы другим инженером без устных пояснений.
- **Риски и зависимости:**
  - Риск «декоративных» runbook-ов без реального прогона.
  - Зависит от BK-203/BK-204, чтобы процедуры соответствовали реальному deployment-контуру.
- **Зависимости:** `BK-203`, `BK-204`.

---

## 4. Приемка и трассировка соответствия

### BK-401. Backend contract + integration/e2e матрица

- **Статус:** `todo`
- **Почему:** для защиты VKR нужна доказуемая матрица покрытия backend-требований F1-F15 с привязкой к автотестам и контрактам.
- **As-is (текущий код):**
  - Уже есть значимый набор contract/integration тестов (`bot`/`scrapper`), включая:
    - contract consistency для OpenAPI/AsyncAPI;
    - HTTP flow и Kafka pipeline тесты;
    - security-тесты `/updates` (401/403/200);
    - outbox retry/failure integration;
    - k8s/CI wiring integration tests.
  - Но отсутствует единый артефакт-матрица покрытия `F1..F15 -> test classes -> статус`.
  - Проверки распределены по модулям и не собраны в единый acceptance dashboard.
- **To-be (целевой контур):**
  - В репозитории есть явная test-matrix по всем backend-требованиям VKR (кроме Android UI части).
  - Для каждого требования указан набор contract/integration/e2e тестов и статус (green/gap).
  - Матрица используется как gate перед релизом/защитой.
- **Декомпозиция реализации:**
  1. Сформировать перечень backend-требований F1-F15 (исключая Android-only сценарии).
  2. Собрать инвентаризацию существующих тестов по модулям (`bot`, `scrapper`, infra).
  3. Построить mapping `требование -> тесты -> тип проверки (contract/integration/e2e)`.
  4. Выделить требования с неполным покрытием и сформировать backlog недостающих тестов.
  5. Добавить/доработать тесты на оба режима доставки (`http|kafka`) и их граничные сценарии.
  6. Зафиксировать outbox failure/retry/recovery path как обязательный acceptance блок.
  7. Зафиксировать security acceptance: `/updates`, bad input, auth/authorization error paths.
  8. Добавить документ матрицы в репозиторий (например, `docs/quality/backend-test-matrix.md`).
  9. Добавить CI-проверку наличия/актуальности матрицы (lightweight consistency check).
- **Тест-план:**
  - matrix audit: каждое F-требование имеет минимум один автоматический тест или явный `gap`.
  - regression: full CI (`verify/linter/integration`) стабильно зеленый после добавления матрицы.
  - negative coverage: проверены не только happy path, но и отказные/ошибочные сценарии.
- **Definition of Done:**
  - Соответствие backend-требованиям VKR подтверждено не «россыпью», а структурированной test-matrix.
  - Для критичных сценариев есть contract + integration (и при необходимости e2e) подтверждение.
  - Acceptance-проверка перед релизом опирается на этот документ как на источник истины.
- **Риски и зависимости:**
  - Риск ложного ощущения покрытия при отсутствии негативных кейсов.
  - Зависит от BK-101..BK-106 и BK-201..BK-204, так как матрица фиксирует финальное состояние фич.
- **Зависимости:** `BK-101..BK-106`, `BK-201..BK-204`.

### BK-402. Финальный traceability (`VKR -> code -> test`)

- **Статус:** `todo`
- **Почему:** финальная защита VKR требует не только тесты, но и трассируемость: каждое требование должно быть связано с кодом и верификацией.
- **As-is (текущий код):**
  - Есть `VKR-backlog.md` и `VKR-plan.md`, но нет финального traceability-артефакта с полным mapping.
  - Ссылки `VKR -> code -> test` не агрегированы в одном месте.
  - Acceptance checklist для защиты не оформлен как отдельный конечный документ.
- **To-be (целевой контур):**
  - В репозитории есть единая карта трассировки `VKR requirement -> implementation modules/files -> test evidence`.
  - Карта включает статус (`implemented`, `tested`, `residual risk`) и ссылку на подтверждающие артефакты.
  - Есть финальный checklist готовности к защите.
- **Декомпозиция реализации:**
  1. Зафиксировать список требований из `VKR.pdf` (функциональные + ключевые нефункциональные для backend).
  2. Для каждого требования указать конкретные файлы/модули реализации (`bot`, `scrapper`, infra).
  3. Добавить ссылки на тестовые классы/сценарии, подтверждающие выполнение требования.
  4. Для спорных/частично закрытых пунктов добавить пометку residual risk и план закрытия.
  5. Собрать единый документ `traceability-matrix` (табличный или структурированный markdown).
  6. Сформировать acceptance checklist для защиты (что показать, какие команды выполнить, какие логи/артефакты предъявить).
  7. Добавить cross-link с BK-401 test-matrix и runbook/CI-CD артефактами.
  8. Провести финальный peer-review трассировки и зафиксировать версию «готово к защите».
- **Тест-план:**
  - traceability review: случайная выборка требований подтверждается открытием кода и запуском релевантных тестов.
  - consistency check: каждый пункт BK-401 имеет ссылку в BK-402 матрице и наоборот.
  - defense dry-run: acceptance checklist выполняется end-to-end без пропусков.
- **Definition of Done:**
  - Backend-соответствие VKR доказуемо единым набором артефактов в репозитории.
  - Для каждого релевантного требования есть цепочка `VKR -> code -> test`.
  - Acceptance checklist готов и применим для демонстрации/защиты.
- **Риски и зависимости:**
  - Риск устаревания трассировки при изменениях кода без обновления матрицы.
  - Напрямую зависит от BK-401 как источника структурированного тестового evidence.
- **Зависимости:** `BK-401`.

---

## Критический путь (backend-only)

1. `BK-101`, `BK-102`, `BK-103`, `BK-104`, `BK-105`, `BK-106`
2. `BK-201`, `BK-202`
3. `BK-203`, `BK-204`
4. `BK-301`, `BK-302`
5. `BK-401`, `BK-402`

---

## Definition of Done (100% backend VKR)

- Функциональные backend-требования VKR закрыты и подтверждены тестами.
- GitHub/StackOverflow интеграции соответствуют целевой модели VKR (conditional polling, poll-state, resilience).
- Доставка уведомлений соответствует целевой архитектуре VKR (`http|kafka`, outbox, security).
- Инфраструктура и деплой полностью воспроизводимы из репозитория (Redis, Kafka+Zookeeper, k8s, CI/CD).
- Документация и acceptance-пакет готовы для защиты.

