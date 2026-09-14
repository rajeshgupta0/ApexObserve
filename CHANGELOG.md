# Changelog

All notable changes to the ApexObserve project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v1.0.0] - 2026-09-14

### Added
- **Microservices Observability Core:** Complete end-to-end telemetry pipeline (api-gateway, ingestion-service, processing-service, query-service, alerting-service) built on Java 21 Spring Boot.
- **OpenTelemetry & Kafka Integration:** Robust telemetry ingestion via OpenTelemetry Collector routing to Kafka (KRaft mode).
- **TimescaleDB Storage:** High-performance, time-series storage for OpenTelemetry traces and metrics in PostgreSQL.
- **AI Intelligence Service:** FastAPI based Python service utilizing Isolation Forests for real-time anomaly detection and Linear Regression for predictive analytics/capacity planning.
- **Intelligent Root Cause & Blast Radius:** Automated algorithms traversing the dependency graph to accurately identify origin faults and map downstream impact.
- **Next.js Frontend Dashboard:** Comprehensive, React-based UI for visualizing traces, metrics, alerts, incidents, and interactive dependency graphs.
- **Synthetic Demo Environment:** Complete suite of instrumented demo microservices (user, order, payment, inventory, notification) with a traffic generator script `generate-traffic.ps1` to inject real, observable failures (e.g., inventory deduction errors, payment latencies).
- **Production-Ready Configuration:** Refactored system architecture to use externalized environment variables (`.env`) for secrets (e.g., PostgreSQL credentials) and cross-service HTTP routing.

### Changed
- Consolidated local startup scripts for streamlined developer experience on Windows (`start-all.ps1`).
- Updated all repository documentation for public GitHub release, improving recruiter and client readability.

### Fixed
- Resolved configuration hardcoding issues across Java, Python, and Next.js projects to ensure environments can be deployed safely without exposing plain-text secrets.
