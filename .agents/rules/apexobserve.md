# ApexObserve — Frozen Architecture Rules

These rules are always-on for all agents working in this repository.

## Stack Constraints

Approved and frozen:
- Java 21, Spring Boot 3, Maven
- Apache Kafka
- PostgreSQL, TimescaleDB
- Redis (cache-only, non-authoritative)
- OpenTelemetry, OTel Collector
- Python, FastAPI, scikit-learn, Prophet where justified
- Next.js, TypeScript, Tailwind, Recharts, React Flow
- Docker, Docker Compose

## Prohibited Without Benchmark Evidence

- ClickHouse
- Elasticsearch / OpenSearch
- Kubernetes production deployment
- Mandatory LLM infrastructure
- Unnecessary microservices, databases, or queues
- D3.js unless genuinely required by the visualization

## Data Correctness

Metric identity: `tenant_id + service_id + metric_name + time + label_hash`

label_hash: deterministic, order-independent, escaped, collision-resistant.

Log identity: no coarse uniqueness key that overwrites distinct events.

## Telemetry Flow

```
Demo App → OTel SDK → OTel Collector → Kafka → Processing/Ingestion → Storage → Query Service → Frontend
```

Topics: telemetry.metrics, telemetry.logs, telemetry.traces (with DLQs).

Do NOT replace real telemetry with mock data.

## Quality

- Evidence over speculation
- Real telemetry over fake data
- Deterministic behavior where possible
- Explainability over opacity
- Minimal architecture
- Explicit failure states
