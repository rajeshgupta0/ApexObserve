# ApexObserve — Complete Roadmap

**Version:** 1.0  
**Status:** Frozen roadmap  
**Phases:** 18

## Strategy

```text
Foundation
↓
Real Telemetry
↓
Metrics
↓
Dashboard + Health
↓
Logs
↓
Tracing
↓
Dependency Graph
↓
Alerting
↓
Correlation
↓
Anomaly Detection
↓
Evidence
↓
RCA
↓
Blast Radius
↓
Incidents
↓
Prediction
↓
Self-Observability
↓
Polish
↓
Benchmarking
```

## Phase 1 — Infrastructure/Foundation
Build the monorepo and runtime foundation: Java 21/Spring Boot 3, Next.js/TypeScript, Python/FastAPI, Kafka, PostgreSQL/TimescaleDB, Redis where needed, OTel Collector, Docker Compose, configuration and health endpoints.

**Gate:** foundation implementation/build/startup verified where environment permits.

## Phase 2 — Demo Microservices
Build gateway, user, order, payment, inventory and notification services with real service communication, OTel instrumentation, metrics/logs/traces, propagation and realistic traffic.

**Gate:** real requests produce real telemetry.

## Phase 3 — Metrics Pipeline
Implement:

```text
OTel → Collector → Kafka → Ingestion → TimescaleDB → Query API
```

Include validation, normalization, enrichment, idempotency, label-aware identity, bounded retry and DLQ.

**Critical:** `label_hash` prevents distinct label combinations from overwriting one another.

## Phase 4 — Metrics Dashboard + Health Score v1
Service overview/detail, KPIs, latency, traffic, error rate, time series, time ranges, deterministic health score, breakdown, and loading/empty/error states.

Health dimensions:

```text
Latency + Traffic + Reliability
```

## Phase 5 — Logs
Implement:

```text
OTel logs → Collector → telemetry.logs → Processing/Ingestion → PostgreSQL → Query API
```

Include structured storage, severity, filtering, pagination, trace/span correlation, validation, error handling and DLQ.

## Phase 6 — Distributed Tracing
Trace ingestion, span storage, querying, detail, waterfall, latency breakdown, errors and telemetry correlation.

D3.js remains conditional.

## Phase 7 — Service Dependency Map
Service graph, dependencies, request relationships, health, latency and error context. React Flow preferred.

## Phase 8 — Alerting
Rules, thresholds, evaluation, persistence, lifecycle, severity, API and UI.

## Phase 9 — Intelligent Alert Correlation
Temporal, service, dependency and similarity correlation; alert grouping and noise reduction.

## Phase 10 — Anomaly Detection
Baselines, statistical/ML anomaly detection, confidence and evidence.

## Phase 11 — Evidence Engine
Evidence model, collection, relationships, ranking and API.

## Phase 12 — Explainable RCA
RCA factors, scoring, confidence, ranked probable causes, evidence and explanations.

Working confidence threshold: **0.70**. Actual factor weights are decided here.

## Phase 13 — Blast Radius
Impacted services, dependency traversal, affected paths/system areas, impact context and visualization.

## Phase 14 — Incident Management
Incident grouping, lifecycle, timeline, evidence, RCA and blast radius.

```text
OPEN → ACKNOWLEDGED → INVESTIGATING → IDENTIFIED → RESOLVING → RESOLVED → REOPENED
```

Working reopen window: **30 minutes**.

## Phase 15 — Prediction
Forecasting, risk prediction, confidence, evidence, time horizon, API and UI. Predictions must communicate uncertainty.

## Phase 16 — Self-Observability
Observe ApexObserve itself: API, ingestion, processing, Kafka, databases, AI, frontend, queue lag, throughput and failures.

## Phase 17 — Final Polish
UX consistency, accessibility, responsive behavior, performance, loading/error/empty states, API consistency, documentation and visual polish.

No major new capability should be introduced here.

## Phase 18 — Testing & Benchmarking
Unit, integration, API, E2E, failure-path and regression tests. Benchmark telemetry throughput, Kafka throughput, ingestion latency, DB writes, query latency, storage growth, dashboard response, anomaly quality, RCA quality and prediction quality.

Use benchmark evidence before reconsidering ClickHouse.

# Phase Gate Methodology

Every phase follows:

```text
UNDERSTAND
↓
INSPECT
↓
IMPLEMENT
↓
UNIT TEST
↓
BUILD / TYPECHECK
↓
INTEGRATION TEST
↓
REAL DATA VERIFICATION
↓
ERROR HANDLING
↓
OBSERVABILITY
↓
DOCUMENTATION
↓
REPORT
↓
STOP
```

Every phase reports **PASS / FAIL / BLOCKED**.

## Current Known State

```text
Phase 1 — NOT STARTED (greenfield; prior implementation not carried over)
Phase 2 — NOT STARTED
Phase 3 — NOT STARTED
Phase 4 — NOT STARTED
Phase 5 — NOT STARTED
Phase 6 — NOT STARTED
Phase 7 — NOT STARTED
Phase 8 — NOT STARTED
Phase 9 — NOT STARTED
Phase 10 — NOT STARTED
Phase 11 — NOT STARTED
Phase 12 — NOT STARTED
Phase 13 — NOT STARTED
Phase 14 — NOT STARTED
Phase 15 — NOT STARTED
Phase 16 — NOT STARTED
Phase 17 — NOT STARTED
Phase 18 — NOT STARTED
```

## Final Rule

**Do not sacrifice correctness for speed.**

ApexObserve must remain a coherent observability platform rather than a collection of disconnected features.
