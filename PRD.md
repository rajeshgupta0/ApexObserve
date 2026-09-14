# ApexObserve — Product Requirements Document (PRD)

**Version:** 1.0  
**Status:** Frozen Product Direction  
**Product:** ApexObserve  
**Category:** AI-powered distributed observability platform

## 1. Product Vision

ApexObserve is an intelligent observability platform for distributed applications and microservices.

> **ApexObserve does not only detect problems. It explains them with evidence, shows what they affected, correlates alerts into meaningful incidents, and provides health scores and predictions.**

The product is built around OpenTelemetry, event-driven processing, explainability, and evidence.

## 2. Problem

Microservice systems generate metrics, logs, traces, and alerts that are often disconnected. Engineers need to determine what broke, when it started, what caused it, what was affected, and whether the problem is likely to recur.

ApexObserve unifies these signals into an operational investigation workflow.

## 3. Goals

1. Collect real Metrics, Logs, and Traces using OpenTelemetry.
2. Reliably process and persist telemetry.
3. Provide service/system health.
4. Detect anomalies and alerts.
5. Correlate related alerts.
6. Build evidence across telemetry types.
7. Provide explainable RCA.
8. Show blast radius.
9. Manage incidents.
10. Provide predictions with confidence and evidence.
11. Observe ApexObserve itself.

## 4. Non-Goals

Currently out of scope:

- Advanced deep learning as the primary intelligence layer
- Required LSTM/Autoencoder systems
- Full autonomous remediation
- Production Kubernetes deployment
- ClickHouse before demonstrated volume need
- Mandatory generative-AI/LLM explanations
- Unnecessary microservices
- Unnecessary infrastructure
- Fake telemetry as a substitute for real telemetry

## 5. Users

### SRE / DevOps Engineer
Investigates service degradation, incidents, dependencies, RCA, and impact.

### Platform Engineer
Needs telemetry correctness and service-level visibility.

### Backend Engineer
Needs logs, traces, dependencies, and request behavior.

### Engineering Manager
Needs high-level health and incident visibility.

## 6. Official MUST-HAVES

1. OpenTelemetry telemetry collection
2. Explainable RCA with evidence
3. Evidence-based AI insights
4. Blast Radius Analysis
5. Intelligent Alert Correlation
6. Incident Grouping & Lifecycle
7. Service & System Health Score
8. Prediction with Confidence + Evidence
9. ApexObserve Self-Observability

## 7. Telemetry Requirements

### Metrics
Support request count/rate, latency, errors, service metrics, and resource metrics where practical.

Metric identity must preserve distinct label combinations. The Phase 3 `label_hash` discriminator prevents coarse identity collisions.

### Logs
Preserve timestamp, severity, body/message, service, environment, tenant, trace ID, span ID, attributes, and resource attributes.

### Traces
Preserve trace ID, span ID, parent relationship, service, operation, timestamps, duration, status, and attributes.

## 8. Intelligence

### Anomaly Detection
Prefer explainable statistical/ML methods such as thresholds, rolling statistics, z-score-style detection, forecasting, scikit-learn, and Prophet where justified.

### Evidence Engine
Connect metrics, logs, traces, alerts, dependencies, incidents, and health into evidence.

### RCA
Use anomaly timing, dependencies, error rate, latency, trace evidence, service health, and alert correlation. Actual weights are decided in Phase 12. Proposed confidence threshold: **0.70**.

## 9. Incident Lifecycle

```text
OPEN → ACKNOWLEDGED → INVESTIGATING → IDENTIFIED → RESOLVING → RESOLVED → REOPENED
```

Proposed reopen window: **30 minutes**.

## 10. Health Score

Health Score v1 is deterministic and explainable, using:

- latency
- traffic
- reliability/error rate

The UI must expose the breakdown.

## 11. Prediction

Prediction must include:

- predicted condition/value
- time horizon
- confidence
- supporting evidence
- service/system context

Predictions are not certainty.

## 12. Frontend

Provide service overview/detail, metrics, logs, traces, dependency map, alerts, incidents, health, RCA, blast radius, and predictions.

Prioritize operational clarity, evidence, drill-down, and correlation.

## 13. Reliability

- bounded retries
- DLQs
- validation
- no silent data loss
- correct telemetry identity
- health endpoints
- operational metrics

## 14. Security

- no hardcoded secrets
- environment configuration
- authentication/authorization
- tenant isolation where applicable
- input validation
- safe DB access
- secret-safe logging

## 15. Success

An engineer can move from:

```text
System health
→ affected service
→ abnormal signal
→ correlated telemetry
→ probable cause
→ evidence
→ blast radius
→ incident
→ resolution
```

## 16. Quality Principles

**Evidence over speculation. Real telemetry over fake data. Deterministic behavior where possible. Explainability over opacity. Minimal architecture. Explicit failure states. Reproducibility. Self-observability.**
