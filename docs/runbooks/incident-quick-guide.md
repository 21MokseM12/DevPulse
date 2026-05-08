# Incident quick guide

Быстрый triage для типовых отказов DevPulse.

## Triage-порядок

1. Зафиксировать время начала и окружение (`staging`/`production`).
2. Оценить blast radius: `bot`, `scrapper`, БД, Kafka, внешние API.
3. Проверить последние релизы и rollout status.
4. Принять решение: mitigation (rollback/scale/restart) или наблюдение.
5. Зафиксировать действия в incident timeline.

## Секреты и безопасная работа

- Секреты брать из штатного хранилища, не из чатов.
- Не вставлять значения секретов в issue/postmortem.
- После security-инцидента выполнить ротацию: `INTERNAL_SHARED_SECRET`, API tokens.

---

## Матрица: симптом -> первичная диагностика -> действие

| Симптом | Первичная диагностика | Действие |
| --- | --- | --- |
| `bot`/`scrapper` не готовы (`CrashLoopBackOff`) | `kubectl -n <ns> get pods`, `kubectl -n <ns> logs <pod>` | Проверить env/secret/configmap, выполнить rollback `rollback-cd-k8s.md` |
| Ошибки соединения с PostgreSQL | `kubectl -n <ns> logs <pod> | rg -i \"postgres|connection\"` | Проверить `*-db` pod, секреты БД, при повреждении данных перейти к `backup-restore-postgres.md` |
| Ошибки Kafka publish/consume | `kubectl -n <ns> logs <pod> | rg -i \"kafka|topic|timeout\"` | Проверить `kafka`/`zookeeper`, статус `kafka-topics-init`, пересоздать топики |
| `401/403` на internal endpoint | Проверить заголовок `INTERNAL_SHARED_HEADER` и секрет `INTERNAL_SHARED_SECRET` | Сверить секреты `bot` и `scrapper`, при рассинхронизации пересоздать secret и redeploy |
| Ошибки внешних API (GitHub/SO) | `kubectl -n <ns> logs <scrapper-pod> | rg -i \"github|stackoverflow|401|429\"` | Проверить токены/лимиты, временно снизить нагрузку, обновить ключи |
| Rollout завис/неуспешен | `kubectl -n <ns> rollout status deployment/<name>` | Выполнить rollback и post-deploy smoke |

---

## Команды первичной диагностики

### Preconditions

- Доступ к namespace инцидента.

### Steps

```bash
NAMESPACE=devpulse-production
kubectl -n "$NAMESPACE" get pods -o wide
kubectl -n "$NAMESPACE" get svc
kubectl -n "$NAMESPACE" get events --sort-by=.metadata.creationTimestamp | tail -n 50
kubectl -n "$NAMESPACE" rollout status deployment/bot --timeout=60s
kubectl -n "$NAMESPACE" rollout status deployment/scrapper --timeout=60s
```

### Expected output

- Понятен слой отказа (приложение/инфра/внешний API/секреты).

### Timeout

- До 15 минут на первичную диагностику.

### Failure actions

- Эскалация дежурному SRE/DevOps.
- Временная mitigation-стратегия: rollback, ограничение трафика, выключение неключевых job.
