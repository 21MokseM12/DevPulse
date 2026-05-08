# Backup/Restore PostgreSQL

Runbook для резервного копирования и восстановления БД `bot-db` и `scrapper-db` в двух контурах: `docker compose` и `k8s`.

## Связанные артефакты

- `docker-compose.yaml`
- `k8s/staging/stateful-services.yaml`
- `k8s/production/stateful-services.yaml`

## Секреты и безопасная работа

- Брать креденшелы только из секрет-хранилища окружения (`.env` для локалки, GitHub/Kubernetes secrets для k8s).
- Не печатать пароли в терминал и логи CI.
- Не сохранять дампы в репозитории и не отправлять в публичные каналы.
- Ротация паролей: после аварийного восстановления обновить секреты БД и выполнить redeploy.

---

## Процедура: backup (compose)

### Preconditions

- Запущены `bot-db` и `scrapper-db` (`docker compose ps`).
- В наличии каталог для дампов, например `./backups`.
- Доступны переменные из `.env`: `BOT_POSTGRES_*`, `SCRAPPER_POSTGRES_*`.

### Steps

```bash
mkdir -p backups
docker compose exec -T bot-db \
  pg_dump -U "${BOT_POSTGRES_USER:-postgres}" -d "${BOT_POSTGRES_DB_NAME:-devpulse_bot}" \
  > "backups/bot-db-$(date +%Y%m%d-%H%M%S).sql"

docker compose exec -T scrapper-db \
  pg_dump -U "${SCRAPPER_POSTGRES_USER:-postgres}" -d "${SCRAPPER_POSTGRES_DB_NAME:-devpulse_scrapper}" \
  > "backups/scrapper-db-$(date +%Y%m%d-%H%M%S).sql"
```

### Expected output

- В `./backups` появилось 2 SQL-файла ненулевого размера.
- `pg_dump` завершился с кодом `0`.

### Timeout

- До 2 минут на каждую БД (зависит от объёма данных).

### Failure actions

- Проверить статус контейнеров: `docker compose ps`.
- Проверить креденшелы и имя БД в `.env`.
- Если диск заполнен, очистить/переместить старые backup.

---

## Процедура: restore (compose)

### Preconditions

- Есть валидный SQL-дамп.
- Есть окно обслуживания (восстановление перезапишет состояние).

### Steps

```bash
docker compose exec -T bot-db \
  psql -U "${BOT_POSTGRES_USER:-postgres}" -d "${BOT_POSTGRES_DB_NAME:-devpulse_bot}" \
  < backups/bot-db-latest.sql

docker compose exec -T scrapper-db \
  psql -U "${SCRAPPER_POSTGRES_USER:-postgres}" -d "${SCRAPPER_POSTGRES_DB_NAME:-devpulse_scrapper}" \
  < backups/scrapper-db-latest.sql
```

### Expected output

- Команды завершились с кодом `0`.
- `SELECT count(*)` на целевых таблицах возвращает ожидаемые значения.

### Timeout

- До 5 минут на БД среднего размера.

### Failure actions

- Проверить, что SQL-дамп не поврежден (`file`, `wc -l`).
- Проверить ограничения по соединениям и блокировки.
- При частичном restore повторить процедуру после очистки схемы.

---

## Процедура: backup/restore (k8s)

### Preconditions

- Доступ к кластеру (`kubectl auth can-i get pods -n <namespace>`).
- Известен namespace: `devpulse-staging` или `devpulse-production`.
- Pod с PostgreSQL запущен и `Ready`.

### Steps (backup)

```bash
NAMESPACE=devpulse-staging
BOT_POD=$(kubectl -n "$NAMESPACE" get pod -l app=bot-db -o jsonpath='{.items[0].metadata.name}')
SCRAPPER_POD=$(kubectl -n "$NAMESPACE" get pod -l app=scrapper-db -o jsonpath='{.items[0].metadata.name}')

kubectl -n "$NAMESPACE" exec "$BOT_POD" -- \
  pg_dump -U "$BOT_POSTGRES_USER" -d "$BOT_POSTGRES_DB_NAME" \
  > "backups/k8s-bot-db-$(date +%Y%m%d-%H%M%S).sql"

kubectl -n "$NAMESPACE" exec "$SCRAPPER_POD" -- \
  pg_dump -U "$SCRAPPER_POSTGRES_USER" -d "$SCRAPPER_POSTGRES_DB_NAME" \
  > "backups/k8s-scrapper-db-$(date +%Y%m%d-%H%M%S).sql"
```

### Steps (restore)

```bash
kubectl -n "$NAMESPACE" exec -i "$BOT_POD" -- \
  psql -U "$BOT_POSTGRES_USER" -d "$BOT_POSTGRES_DB_NAME" \
  < backups/k8s-bot-db-latest.sql

kubectl -n "$NAMESPACE" exec -i "$SCRAPPER_POD" -- \
  psql -U "$SCRAPPER_POSTGRES_USER" -d "$SCRAPPER_POSTGRES_DB_NAME" \
  < backups/k8s-scrapper-db-latest.sql
```

### Expected output

- Дампы выгружены/загружены без ошибок.
- После restore сервисы `bot` и `scrapper` проходят health-check.

### Timeout

- До 10 минут на полную процедуру по двум БД.

### Failure actions

- Проверить pod restart/eviction (`kubectl get pods -o wide`).
- Проверить секреты с логинами/паролями и права доступа.
- При ошибках целостности повторить restore с последнего корректного дампа.
