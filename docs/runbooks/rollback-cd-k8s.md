# Rollback CD/Kubernetes

Runbook для отката релиза в `staging` и `production` после неуспешного деплоя или деградации.

## Связанные артефакты

- `.github/workflows/cd-staging.yaml`
- `.github/workflows/cd-production.yaml`
- `k8s/staging/bot.yaml`, `k8s/staging/scrapper.yaml`
- `k8s/production/bot.yaml`, `k8s/production/scrapper.yaml`

## Секреты и безопасная работа

- Не публиковать `KUBE_CONFIG_*` и application secrets в логах.
- Любые ручные rollback-операции выполнять только с ролями deployer/on-call.
- После rollback обновить incident timeline и релизный артефакт.

---

## Процедура: автоматический rollback (pipeline)

### Preconditions

- Запущен `cd-staging` или `cd-production`.
- Пайплайн завершился с ошибкой на шагах rollout/health.

### Steps

1. Открыть failed run в GitHub Actions.
2. Убедиться, что шаг `Rollback on failure` выполнен.
3. Проверить `kubectl rollout history` для `deployment/bot` и `deployment/scrapper`.

### Expected output

- Deployment ревизии откатились на предыдущую рабочую.
- Новые pod в `Running/Ready`.

### Timeout

- До 5 минут после срабатывания шага rollback.

### Failure actions

- Выполнить ручной rollback (следующий раздел).
- Если rollback невозможен (нет предыдущей revision), выполнить redeploy последнего стабильного `image_tag`.

---

## Процедура: ручной rollback (staging/prod)

### Preconditions

- Подтвержден инцидент или health degradation после релиза.
- Есть доступ к namespace (`devpulse-staging` или `devpulse-production`).

### Steps

```bash
NAMESPACE=devpulse-staging
kubectl -n "$NAMESPACE" rollout undo deployment/bot
kubectl -n "$NAMESPACE" rollout undo deployment/scrapper
kubectl -n "$NAMESPACE" rollout status deployment/bot --timeout=180s
kubectl -n "$NAMESPACE" rollout status deployment/scrapper --timeout=180s
```

Если нужен откат на конкретную ревизию:

```bash
kubectl -n "$NAMESPACE" rollout history deployment/bot
kubectl -n "$NAMESPACE" rollout undo deployment/bot --to-revision=<REVISION>
```

### Expected output

- `rollout status` возвращает `successfully rolled out`.
- Ошибочные pod старой ревизии сняты с трафика.

### Timeout

- До 3 минут на один deployment.

### Failure actions

- Проверить события: `kubectl -n "$NAMESPACE" describe pod <pod-name>`.
- Проверить образ: `kubectl -n "$NAMESPACE" get deploy bot -o yaml | rg image`.
- Выполнить pin known-good image:

```bash
kubectl -n "$NAMESPACE" set image deployment/bot bot=ghcr.io/<owner>/devpulse-bot:<known-good-tag>
kubectl -n "$NAMESPACE" set image deployment/scrapper scrapper=ghcr.io/<owner>/devpulse-scrapper:<known-good-tag>
```

---

## Пост-rollback проверка

### Preconditions

- Rollback завершился без ошибок.

### Steps

```bash
kubectl -n "$NAMESPACE" get pods
kubectl -n "$NAMESPACE" get hpa
kubectl -n "$NAMESPACE" port-forward service/bot 18080:8080 >/tmp/bot-pf.log 2>&1 &
PF_PID=$!
sleep 8
curl --fail --silent http://127.0.0.1:18080/actuator/health
kill "$PF_PID"
```

### Expected output

- Все pod `Ready`.
- `/actuator/health` отвечает `UP`.

### Timeout

- До 10 минут с момента решения об откате.

### Failure actions

- Эскалировать в incident channel.
- Перейти к runbook `incident-quick-guide.md`.
