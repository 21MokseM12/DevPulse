# Post-deploy smoke

Минимальный post-deploy чеклист для `staging` и `production`.

## Связанные артефакты

- `.github/workflows/cd-staging.yaml`
- `.github/workflows/cd-production.yaml`
- `k8s/staging/README.md`
- `k8s/production/README.md`

## Секреты и безопасная работа

- Не логировать значения `INTERNAL_SHARED_SECRET`, `GITHUB_TOKEN`, `SO_ACCESS_TOKEN`.
- Для ручных API-проверок использовать временные токены/ограниченные ключи.

---

## Процедура: smoke после rollout

### Preconditions

- Rollout `bot` и `scrapper` завершен в целевом namespace.
- Доступны `kubectl` и локальный порт для `port-forward`.

### Steps

1. Проверить состояние инфраструктуры:

```bash
NAMESPACE=devpulse-staging
kubectl -n "$NAMESPACE" get pods
kubectl -n "$NAMESPACE" get svc
kubectl -n "$NAMESPACE" get hpa
```

2. Проверить health `bot`:

```bash
kubectl -n "$NAMESPACE" port-forward service/bot 18080:8080 >/tmp/smoke-bot-pf.log 2>&1 &
PF_BOT=$!
sleep 8
curl --fail --silent http://127.0.0.1:18080/actuator/health
kill "$PF_BOT"
```

3. Проверить health `scrapper`:

```bash
kubectl -n "$NAMESPACE" port-forward service/scrapper 18081:8081 >/tmp/smoke-scrapper-pf.log 2>&1 &
PF_SCRAPPER=$!
sleep 8
curl --fail --silent http://127.0.0.1:18081/actuator/health
kill "$PF_SCRAPPER"
```

4. Проверить Kafka bootstrap topics:

```bash
kubectl -n "$NAMESPACE" get jobs
kubectl -n "$NAMESPACE" logs job/kafka-topics-init --tail=50
```

### Expected output

- Все критичные pod в `Running` и `Ready`.
- Оба health endpoint возвращают `UP`.
- Kafka topic init job завершен в `Complete`.

### Timeout

- До 10 минут на полный smoke-проход.

### Failure actions

- Если infra-компоненты не готовы: проверить `describe pod` и `events`.
- Если health endpoint не отвечает: выполнить rollback по `rollback-cd-k8s.md`.
- Если Kafka job неуспешен: переисполнить `kafka-topics-job.yaml`, затем повторить smoke.

---

## Минимальный API smoke

### Preconditions

- Сервис `bot` доступен локально через `port-forward`.

### Steps

```bash
curl --fail --silent http://127.0.0.1:18080/swagger-ui
```

### Expected output

- Страница Swagger доступна (код 200).

### Timeout

- До 1 минуты.

### Failure actions

- Проверить ingress/service routing.
- Проверить логи `bot` и `scrapper`.
