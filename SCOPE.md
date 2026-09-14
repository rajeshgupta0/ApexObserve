# ApexObserve — Scope

**Version:** 1.0  
**Status:** Frozen project scope

## 1. In Scope

```text
Telemetry Collection
→ Processing
→ Storage
→ Query
→ Visualization
→ Detection
→ Correlation
→ Evidence
→ RCA
→ Blast Radius
→ Incidents
→ Prediction
→ Self-Observability
```

## 2. Required Telemetry

### Metrics
Request count/rate, latency, errors, service metrics, resource metrics where practical.

### Logs
Structured logs, severity, body, service, environment, timestamps, trace/span correlation, attributes.

### Traces
Trace/span IDs, parent relationships, service/operation, duration, status, attributes.

## 3. Required Services

Core:

- api-gateway
- ingestion-service
- processing-service boundary
- alerting-service
- query-service
- common

AI:

- ai-service

Demo:

- api-gateway-demo
- user
- order
- payment
- inventory
- notification

Do not unnecessarily split the Processing Service boundary into separate deployables.

## 4. Approved Technology

- Java 21
- Spring Boot 3
- Apache Kafka
- PostgreSQL
- TimescaleDB
- Redis cache-only where needed
- Python
- FastAPI
- scikit-learn
- Prophet/statistical methods where appropriate
- Next.js
- TypeScript
- Tailwind
- Recharts
- React Flow
- D3.js only if justified
- Docker
- Docker Compose
- OpenTelemetry Collector

## 5. Explicitly Out of Scope

- production Kubernetes
- autonomous remediation
- mandatory LLM/Generative AI
- deep learning as primary approach
- ClickHouse before demonstrated need
- unnecessary microservices
- unnecessary stores
- fake telemetry

## 6. Phase Scope

### Phase 1 — Infrastructure/Foundation
Monorepo, Java/Next/Python foundations, Kafka, DB, Redis if needed, OTel Collector, Docker Compose, configuration, health endpoints, documentation.

### Phase 2 — Demo Microservices
Real services, communication, OTel instrumentation, metrics/logs/traces, propagation, realistic traffic, structured logs.

### Phase 3 — Metrics Pipeline
OTel → Collector → Kafka → Ingestion → TimescaleDB → Query API, with validation, enrichment, normalization, idempotency, retries, DLQ.

### Phase 4 — Metrics Dashboard + Health Score v1
Overview/detail, KPIs, time series, ranges, health score, breakdown, real error rate, loading/empty/error states.

### Phase 5 — Logs
OTel logs → Collector → `telemetry.logs` → Processing/Ingestion → PostgreSQL → Query API, including structure, filtering, pagination, correlation, validation, errors, DLQ.

### Phase 6 — Distributed Tracing
Trace ingestion, span storage/query, trace detail, waterfall, service/operation analysis.

### Phase 7 — Service Dependency Map
Service graph, dependencies, request relationships, latency/error context.

### Phase 8 — Alerting
Rules, thresholds, evaluation, persistence, lifecycle, severity, API, UI.

### Phase 9 — Intelligent Alert Correlation
Temporal, service, dependency and similarity correlation; grouping and noise reduction.

### Phase 10 — Anomaly Detection
Baselines, anomaly detection, statistical/ML methods, confidence, evidence.

### Phase 11 — Evidence Engine
Evidence model, collection, relationships, ranking, API.

### Phase 12 — Explainable RCA
Factors, scoring, confidence, ranked causes, evidence, explanation, API/UI.

### Phase 13 — Blast Radius
Impacted services, dependency traversal, affected paths/areas, context and visualization.

### Phase 14 — Incident Management
Grouping, lifecycle, timeline, evidence, RCA, blast radius.

Lifecycle:
```text
OPEN → ACKNOWLEDGED → INVESTIGATING → IDENTIFIED → RESOLVING → RESOLVED → REOPENED
```

### Phase 15 — Prediction
Forecasting, risk prediction, confidence, evidence, horizon, API/UI.

### Phase 16 — Self-Observability
ApexObserve health, ingestion/Kafka/DB/AI/frontend operational health, throughput, queue lag and failures.

### Phase 17 — Final Polish
UX, accessibility, performance, states, consistency, documentation.

### Phase 18 — Testing & Benchmarking
Unit, integration, E2E, failure-path, regression, throughput, latency, storage, AI evaluation.

## 7. Scope Change Rule

Features outside the current phase require explicit review. No silent scope expansion.

## 8. Done Criteria

A phase is done only when implementation, tests, available build/typecheck, integration, real-data verification, error handling, observability, documentation, and a completion report are addressed. Unavailable infrastructure is reported as BLOCKED.
