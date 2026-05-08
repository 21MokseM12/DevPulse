# Production Kubernetes

## Состав

- `namespace.yaml` — namespace `devpulse-production`.
- `configmap.yaml` — несекретные runtime-параметры.
- `secret.example.yaml` — шаблон секретов (заменить значениями из production vault).
- `stateful-services.yaml` — in-cluster PostgreSQL, Redis, Zookeeper, Kafka.
- `kafka-topics-job.yaml` — bootstrap критичных Kafka-топиков.
- `bot.yaml` / `scrapper.yaml` — Deployments, Services, HPA, rolling update и probes.

## Деплой

```bash
kubectl apply -f k8s/production/namespace.yaml
kubectl apply -f k8s/production/configmap.yaml
kubectl apply -f k8s/production/secret.example.yaml
kubectl apply -f k8s/production/stateful-services.yaml
kubectl apply -f k8s/production/kafka-topics-job.yaml
kubectl apply -f k8s/production/bot.yaml
kubectl apply -f k8s/production/scrapper.yaml
```

## Проверка

```bash
kubectl -n devpulse-production get pods
kubectl -n devpulse-production get hpa
kubectl -n devpulse-production get svc
```

## Rollback

```bash
kubectl -n devpulse-production rollout undo deployment/bot
kubectl -n devpulse-production rollout undo deployment/scrapper
```
