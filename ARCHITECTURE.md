# ApexObserve — Architecture

**Version:** 1.0  
**Status:** Frozen Architecture Baseline

## 1. System Architecture

```text
Demo Services
      │
      ▼
OpenTelemetry SDK
      │
      ▼
OTel Collector
      │
      ├──────── telemetry.metrics ────────┐
      ├──────── telemetry.logs ───────────┤
      └──────── telemetry.traces ─────────┤
                                           ▼
                                         Kafka
                                           │
                                           ▼
                              Processing / Ingestion Boundary
                                  │                 │
                                  ▼                 ▼
                              TimescaleDB       PostgreSQL
                               Metrics          Logs/Traces
                                  │                 │
                                  └───────┬─────────┘
                                          ▼
                                    Query Service
                                          │
                                          ▼
                                      Next.js UI
```

## 2. Backend

### API Gateway
External entry point, routing, authentication boundary, validation.

### Ingestion Service
Consumes telemetry, validates, normalizes, enriches, persists, retries, and DLQs.

### Processing Service Boundary
One internal processing boundary for metrics/logs/traces. Do not create three deployables without approval.

### Query Service
Telemetry queries, filters, pagination, aggregation, typed responses.

### Alerting Service
Rules, evaluation, persistence, lifecycle, alert emission.

### AI Service
Anomaly detection, evidence analysis, RCA, prediction. It must not replace deterministic telemetry processing.

## 3. Data Stores

### PostgreSQL
Logs v1, traces v1, application metadata, alerts, incidents, evidence, RCA, prediction metadata.

### TimescaleDB
Metrics and time-series operations.

### Redis
Cache-only and non-authoritative unless explicitly approved otherwise.

## 4. Kafka

Primary event backbone:

```text
telemetry.metrics
telemetry.logs
telemetry.traces
```

Use corresponding DLQs for unrecoverable messages. Exact partitions and consumer groups follow `KAFKA.md`.

## 5. Telemetry Flows

### Metrics
```text
Application → OTel SDK → Collector → Kafka → validation → enrichment → normalization → TimescaleDB → Query API
```

### Logs
```text
Application → OTel SDK → Collector → Kafka → validation → enrichment → PostgreSQL → Query API
```

### Traces
```text
Application → OTel SDK → Collector → Kafka → validation → enrichment → PostgreSQL → Query API
```

## 6. Metrics Identity

Metric identity must include a label discriminator:

```text
tenant_id + service_id + metric_name + time + label_hash
```

`label_hash` is deterministic, order-independent, escaped, and collision-resistant.

## 7. Log Identity

Do not use a coarse uniqueness key that can overwrite distinct log events. Deduplication must use a defensible event identity.

## 8. Correlation

Preserve `trace_id` and `span_id` across relevant telemetry where naturally available.

This enables:

```text
Metric anomaly → related trace → related log
```

and eventually:

```text
Alert → Incident → Evidence → RCA
```

## 9. Intelligence

```text
Telemetry
   ↓
Detection
   ↓
Anomaly / Alert
   ↓
Correlation
   ↓
Evidence Engine
   ↓
RCA
   ↓
Blast Radius
   ↓
Incident
   ↓
Prediction / Risk
```

Underlying evidence must be retained.

## 10. RCA

Frozen factors include anomaly timing, dependency relationships, error rate, latency, trace evidence, service health, and alert correlation. Numerical weights are decided in Phase 12. Working confidence threshold: **0.70**.

## 11. Incident Lifecycle

```text
OPEN → ACKNOWLEDGED → INVESTIGATING → IDENTIFIED → RESOLVING → RESOLVED → REOPENED
```

Working reopen window: **30 minutes**.

## 12. Health Score

Deterministic and explainable. v1 dimensions:

- latency
- traffic
- reliability/error rate

Expose the breakdown.

## 13. AI

Python + FastAPI. Prefer statistical analysis, scikit-learn, forecasting/statistical methods, and Prophet where justified. Deep learning is not primary. LLM explanations are optional future scope.

## 14. Frontend

Next.js + TypeScript + Tailwind.

Use:

- Recharts
- React Flow
- D3.js only when justified

Core views include services, metrics, logs, traces, dependency map, alerts, incidents, RCA, blast radius, predictions, and self-observability.

## 15. Reliability

Handle malformed messages, deserialization errors, validation failures, processing failures, and persistence failures with bounded retry and DLQ rather than infinite retry.

## 16. Security

No hardcoded credentials. Use environment configuration, authentication/authorization, tenant isolation where applicable, safe query construction, and secret-safe logging.

## 17. Deployment

Development/demo uses Docker Compose. Production Kubernetes is out of scope for the current roadmap.

## 18. Architectural Constraints

Do not introduce ClickHouse without benchmark evidence, Elasticsearch/OpenSearch, unnecessary microservices, unnecessary queues/stores, mandatory LLM infrastructure, or production Kubernetes.

Any architecture change requires explicit approval.

## 19. Quality Rule

> **Minimal, event-driven, observable, explainable, and evidence-backed.**
