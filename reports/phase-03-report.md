# Phase 3 Report: Metrics Pipeline

**Objective:** Implement the real metrics pipeline consuming from Kafka (`telemetry.metrics`), validating/enriching in `ingestion-service`, and persisting into TimescaleDB via `processing-service` with correct data collision handling via `label_hash`.

**Status:** PASS

## Implementation
* **Kafka Ingestion:** Created `MetricsIngestionService` in `ingestion-service` with a `@KafkaListener` subscribed to `telemetry.metrics`.
* **Kafka Validation/Routing:** Ingestion service forwards validated metrics to `telemetry.metrics.validated`.
* **Database Entity:** Implemented `MetricEntity` representing the TimescaleDB hypertable layout (including `tenant_id`, `service_id`, `metric_name`, `label_hash`, and `time` as compound primary key) with JSONB labels.
* **Label Hash Generation:** Implemented `LabelHasher` utility ensuring deterministic canonicalization (using `TreeMap`), robust escaping, and SHA-256 hashing to generate unique `label_hash` IDs for metrics. This prevents label variation collisions in TimescaleDB.
* **Persistence:** Created `MetricsProcessingService` in `processing-service` subscribing to `telemetry.metrics.validated` and saving metrics using Spring Data JPA.

## Architecture Compliance
* Adheres to the pipeline definition: OTel -> Kafka -> Ingestion -> Processing -> TimescaleDB.
* Solves the metric uniqueness constraint without race conditions.

## Tests Written
* Unit tests for `LabelHasher` (`LabelHasherTest`):
    * `testDeterminism`
    * `testOrdering`
    * `testDifferentLabels`
    * `testEmptyLabels`
    * `testDelimiterSafety`

## Tests Executed
* Backend Tests: `mvnw.cmd test -B` (Ran 5 tests in `processing-service`, all passed).

## Build Result
* SUCCESS

## Next Steps
* Proceed to Phase 4 (Metrics Dashboard + Health Score) to expose these persisted metrics via `query-service` and API Gateway to the Next.js frontend.
