# Defense Acceptance Checklist (Backend)

Чеклист демонстрации готовности backend-части VKR.

## 1) Требования и трассировка

- [ ] Открыть `docs/quality/backend-test-matrix.md` и показать покрытие `F1..F15`.
- [ ] Открыть `docs/quality/traceability-matrix.md` и показать цепочку `VKR -> code -> test`.
- [ ] Отдельно показать `F12` как известный `residual-risk` backend-only контура.

## 2) Сборка и качество

- [ ] Выполнить `./mvnw clean verify`.
- [ ] Подтвердить green-статус unit/integration/contract и quality-gates.

## 3) Контракты и интеграции

- [ ] Показать контрактные проверки:
  - `bot/src/test/java/backend/academy/bot/contracts/ContractConsistencyTest.java`
  - `scrapper/src/test/java/backend/academy/scrapper/contracts/ContractConsistencyTest.java`
- [ ] Показать интеграции доставки:
  - HTTP: `BotHttpLinkFlowIntegrationTest`
  - Kafka/outbox: `BotKafkaNotificationPipelineIntegrationTest`, `KafkaOutboxProcessorIntegrationTest`
- [ ] Показать security acceptance `/updates`: `InternalUpdatesAuthIntegrationTest`.

## 4) Инфраструктура и эксплуатация

- [ ] Показать k8s/CI wiring:
  - `GithubCdWorkflowIntegrationTest`
  - `KubernetesManifestWiringIntegrationTest`
- [ ] Показать эксплуатационный пакет:
  - `docs/runbooks/backup-restore-postgres.md`
  - `docs/runbooks/rollback-cd-k8s.md`
  - `docs/runbooks/post-deploy-smoke.md`
  - `docs/runbooks/incident-quick-guide.md`
- [ ] Показать `README.md` + `.env.example` как reproducible путь запуска.

## 5) Команды для dry-run защиты

```bash
./mvnw clean verify
```

```bash
docker compose up --build
curl --fail --silent http://localhost:8080/actuator/health
curl --fail --silent http://localhost:8081/actuator/health
```

## 6) Финальный выходной критерий

- [ ] Для каждого backend-требования есть доказуемая запись в traceability matrix.
- [ ] Все обязательные проверки воспроизводимы без устных шагов.
