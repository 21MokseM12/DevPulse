# Staging Kubernetes

## Состав

- `namespace.yaml` — namespace `devpulse-staging`.
- `configmap.yaml` — несекретные runtime-настройки сервисов.
- `secret.example.yaml` — шаблон секретов (создать реальный `Secret` перед деплоем).
- `stateful-services.yaml` — in-cluster PostgreSQL, Redis, Zookeeper, Kafka.
- `kafka-topics-job.yaml` — bootstrap критичных Kafka-топиков.
- `bot.yaml` / `scrapper.yaml` — Deployments, Services, HPA и probes.

## Деплой

```bash
kubectl apply -f k8s/staging/namespace.yaml
kubectl apply -f k8s/staging/configmap.yaml
kubectl apply -f k8s/staging/secret.example.yaml
kubectl apply -f k8s/staging/stateful-services.yaml
kubectl apply -f k8s/staging/kafka-topics-job.yaml
kubectl apply -f k8s/staging/bot.yaml
kubectl apply -f k8s/staging/scrapper.yaml
```

## Проверка

```bash
kubectl -n devpulse-staging get pods
kubectl -n devpulse-staging get hpa
kubectl -n devpulse-staging get svc
```

## Rollback

```bash
kubectl -n devpulse-staging rollout undo deployment/bot
kubectl -n devpulse-staging rollout undo deployment/scrapper
```
