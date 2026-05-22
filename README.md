# Link Tracker

Backend-платформа для отслеживания обновлений по ссылкам (GitHub и StackOverflow) с доставкой уведомлений в сервис `bot`.

## Runtime и архитектура

- Язык и платформа: `Java 23`, `Spring Boot 3`.
- Сервисы:
  - `scrapper` — планировщик polling, нормализация событий и доставка обновлений.
  - `bot` — API пользовательского контура и приёмник внутренних обновлений.
- Инфраструктура: `PostgreSQL` (2 БД), `Kafka + Zookeeper`, `Redis`.
- Миграции: `Liquibase`.
- Поддерживаемые режимы доставки `scrapper -> bot`: `http` и `kafka` (переключение через `SCRAPPER_DELIVERY_MODE`).

Поток обработки в runtime:
1. Пользователь регистрирует клиента и ссылки через `bot`.
2. `scrapper` выбирает ссылки для polling, читает внешние API и вычисляет дельту изменений.
3. События доставки отправляются в `bot` по `http` или через `kafka`.
4. `bot` сохраняет/обогащает контекст и доставляет уведомления в пользовательский канал.

## Быстрый старт (clone -> configure -> run -> verify)

1. Склонировать репозиторий.
2. Создать локальный конфиг из шаблона:

   ```bash
   cp .env.example .env
   ```
3. Заполнить обязательные внешние токены в `.env`:
   - `GITHUB_TOKEN`
   - `SO_TOKEN_KEY`
   - `SO_ACCESS_TOKEN`
4. Поднять локальный контур:

   ```bash
   docker compose up --build
   ```
5. Проверить проект quality-gates и тестами:

   ```bash
   ./mvnw clean verify
   ```

Шаблон переменных хранится в `./.env.example` и является источником истины для локального запуска backend.

## Проверка здоровья и smoke

Проверка readiness/liveness после старта:

```bash
curl --fail --silent http://localhost:8080/actuator/health
curl --fail --silent http://localhost:8081/actuator/health
```

Базовый smoke-сценарий:

1. Убедиться, что оба health endpoint возвращают `UP`.
2. Открыть Swagger:
   - `http://localhost:8080/swagger-ui`
   - `http://localhost:8081/swagger-ui`
3. Открыть Kafka UI:
   - `http://localhost:8082`
4. Проверить, что контейнеры `bot`, `scrapper`, `bot-db`, `scrapper-db`, `kafka`, `redis` в состоянии `healthy`.

## Конфигурация окружений

### Локально через docker compose

- Используются значения из `.env` (или дефолты в `docker-compose.yaml`).
- Поднимаются сервисы приложения, БД и брокер сообщений.
- Kafka UI доступен локально по адресу `http://localhost:8082`.
- Для Kafka создаются топики через `kafka-init`.

### Kubernetes (`staging` / `production`)

- Манифесты: `k8s/staging` и `k8s/production`.
- Деплой выполняется workflow-ами:
  - CI: `.github/workflows/build.yaml`
  - CD staging: `.github/workflows/cd-staging.yaml`
  - CD production: `.github/workflows/cd-production.yaml`
- Для CD требуются kubeconfig secrets (`KUBE_CONFIG_STAGING`, `KUBE_CONFIG_PRODUCTION`) и секреты приложений/БД соответствующего окружения.

## Режимы логирования

- По умолчанию сервисы пишут человекочитаемые логи через `logback-dev.xml`.
- Для production-профиля включается JSON-логирование через `logback-prod.xml`.
- Переключение:

  ```bash
  SPRING_PROFILES_ACTIVE=prod java -jar bot/target/bot-*.jar
  SPRING_PROFILES_ACTIVE=prod java -jar scrapper/target/scrapper-*.jar
  ```
- В JSON-логах присутствуют поля: `timestamp`, `level`, `service`, `traceId`, `spanId`, `requestId`, `message`.
- Значения с ключами `password`, `secret`, `token`, `apiKey` автоматически маскируются в логе как `***`.

## Режимы доставки `http|kafka`

### Переключение режима

- Ключ: `SCRAPPER_DELIVERY_MODE`.
- Допустимые значения:
  - `http` — прямая доставка `scrapper -> bot` по внутреннему API.
  - `kafka` — асинхронная доставка через topic `link-updates`.
- Значение по умолчанию: `http`.

### Что нужно задать в окружении

- Для `http`:
  - `SCRAPPER_BOT_URL` (или `BOT_URL`) — адрес `bot`.
  - `INTERNAL_SHARED_SECRET` + заголовок (`SCRAPPER_AUTH_HEADER` / `INTERNAL_SHARED_HEADER`) для внутренней авторизации.
- Для `kafka`:
  - `SCRAPPER_KAFKA_BOOTSTRAP_SERVERS` / `KAFKA_BOOTSTRAP_SERVERS`.
  - `SCRAPPER_OUTBOX_TOPIC`, `BOT_LINK_UPDATES_TOPIC` (должны ссылаться на один и тот же topic).
  - `BOT_LINK_UPDATES_GROUP_ID` для изоляции consumer-группы `bot`.

### Поведение и ограничения

- `http`: минимальная задержка доставки, но выше чувствительность к временной недоступности `bot`.
- `kafka`: буферизация и устойчивость к краткосрочным сбоям `bot`, но доставка становится eventual-consistent.
- При `kafka` важно контролировать lag consumer-группы и состояние outbox-процессора.

## Resilience: timeout/retry/backoff/circuit breaker

### Runtime-политики в backend

- Retry + Circuit Breaker + Rate Limiter применяются в `scrapper` для `github-api`, `stackoverflow-api`, `bot-api`.
- Базовые значения по умолчанию (`scrapper/src/main/resources/application.yaml`):
  - `retry.max-attempts=3`, `retry.wait-duration=500ms`;
  - `circuitbreaker.failure-rate-threshold=50`, `wait-duration-in-open-state=30s`;
  - `ratelimiter.limit-for-period=10`, `limit-refresh-period=1s`, `timeout-duration=0`;
  - polling-backoff после ошибок: экспоненциальный (base -> max) через `poll_state.next_retry_at`.

### Рекомендуемые значения для production

- Timeout внешних вызовов: 3-5s на запрос (через HTTP client/ingress policy).
- Retry: 3-4 попытки, стартовая пауза 300-700ms, экспоненциальный backoff без бесконечных ретраев.
- Circuit breaker: окно 20-50 запросов, порог срабатывания 40-60%, `open-state` 20-60s.
- Polling backoff: старт от `force-check-delay`, верхняя граница не менее x16-x32 от base.

### Как проверить, что политика активна

1. Выполнить `./mvnw clean verify` (проверяет unit/integration/contract и quality-gates).
2. Для локального smoke после запуска:
   - `curl --fail --silent http://localhost:8080/actuator/health`
   - `curl --fail --silent http://localhost:8081/actuator/health`
3. Для post-deploy сценария использовать `docs/runbooks/post-deploy-smoke.md`.
4. При диагностике деградации сверять `retry/circuitbreaker` метрики и lag Kafka consumer-групп.

## Миграция конфигурации: compose -> k8s

- `compose`: переменные загружаются из `.env`.
- `k8s`: те же ключи раскладываются по:
  - `Secret` — чувствительные значения (`GITHUB_TOKEN`, `SO_ACCESS_TOKEN`, `INTERNAL_SHARED_SECRET`, пароли БД).
  - `ConfigMap` — не секретные параметры (`SCRAPPER_DELIVERY_MODE`, адреса сервисов, Kafka topic/group значения и т.д.).
- Имена env-переменных сохраняются одинаковыми, чтобы не менять `application.yaml`.

## Эксплуатационная документация

- Базовый пакет runbook-ов: `docs/runbooks/README.md` (развивается в рамках `BK-302`).
- Kubernetes-специфика:
  - `k8s/staging/README.md`
  - `k8s/production/README.md`

## Дополнительно

- Дополнительные материалы: [HELP.md](./HELP.md)

