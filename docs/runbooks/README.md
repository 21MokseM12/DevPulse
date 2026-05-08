# Runbooks

Пакет эксплуатационных runbook-документов для backend DevPulse.

## Содержание

- [Backup/Restore PostgreSQL](./backup-restore-postgres.md)
- [Rollback CD/Kubernetes](./rollback-cd-k8s.md)
- [Post-deploy smoke](./post-deploy-smoke.md)
- [Incident quick guide](./incident-quick-guide.md)

## Привязка к pipeline и манифестам

- CD staging: `.github/workflows/cd-staging.yaml`
- CD production: `.github/workflows/cd-production.yaml`
- Kubernetes manifests: `k8s/staging/*`, `k8s/production/*`

## Tabletop-проверка (артефакт)

Проверка runbook-пакета выполнена в формате tabletop:

- сценарий backup/restore для `bot-db` и `scrapper-db` пройден по шагам;
- сценарий rollback deployment для `staging` пройден по шагам;
- post-deploy smoke чеклист использован как критерий выхода после rollback.

Фиксация результата:

- дата: `2026-05-08`;
- формат: walkthrough по документам без устных шагов;
- результат: шаги воспроизводимы, критичных пробелов не выявлено.
