# Link Tracker

Backend-платформа для отслеживания обновлений по ссылкам (GitHub и StackOverflow) с доставкой уведомлений в сервис `bot`.

## Runtime и архитектура

- Язык и платформа: `Java 23`, `Spring Boot 3`.
- Сервисы:
  - `scrapper` — планировщик и обработчик изменений по подпискам.
  - `bot` — API/приёмник уведомлений и пользовательский контур.
- Инфраструктура: `PostgreSQL` (2 БД), `Kafka + Zookeeper`, `Redis`.
- Миграции: `Liquibase`.
- Поддерживаемые режимы доставки `scrapper -> bot`: `http` и `kafka` (переключение через `SCRAPPER_DELIVERY_MODE`).

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
   mvn clean verify
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
3. Проверить, что контейнеры `bot`, `scrapper`, `bot-db`, `scrapper-db`, `kafka`, `redis` в состоянии `healthy`.

## Конфигурация окружений

### Локально через docker compose

- Используются значения из `.env` (или дефолты в `docker-compose.yaml`).
- Поднимаются сервисы приложения, БД и брокер сообщений.
- Для Kafka создаются топики через `kafka-init`.

### Kubernetes (`staging` / `production`)

- Манифесты: `k8s/staging` и `k8s/production`.
- Деплой выполняется workflow-ами:
  - CI: `.github/workflows/build.yaml`
  - CD staging: `.github/workflows/cd-staging.yaml`
  - CD production: `.github/workflows/cd-production.yaml`
- Для CD требуются kubeconfig secrets (`KUBE_CONFIG_STAGING`, `KUBE_CONFIG_PRODUCTION`) и секреты приложений/БД соответствующего окружения.

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

