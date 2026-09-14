# ApexObserve

ApexObserve is an advanced observability and monitoring platform built with a microservices architecture. It provides robust telemetry ingestion, real-time alerting, dependency mapping, intelligent incident resolution, and AI-driven predictive insights for complex, distributed systems.

## Architecture

ApexObserve utilizes a highly scalable polyglot architecture:
- **Frontend:** Next.js (React) application for a responsive, modern dashboard.
- **Backend Microservices (Java/Spring Boot):**
  - `api-gateway`: Unified entrypoint for management and querying.
  - `ingestion-service`: Consumes OpenTelemetry traces and metrics from Kafka.
  - `processing-service`: Correlates metrics, detects anomalies, and normalizes telemetry.
  - `query-service`: Powers complex queries, dependency graphs, and health scores.
  - `alerting-service`: Evaluates rules and manages incident lifecycles.
- **AI Service (Python/FastAPI):** Provides advanced telemetry intelligence, including root cause analysis, blast radius prediction, and isolation forest-based anomaly detection.
- **Infrastructure:**
  - Kafka (KRaft mode) for high-throughput message streaming.
  - PostgreSQL with TimescaleDB extension for time-series telemetry storage.
  - Redis for caching and rate limiting.
  - OpenTelemetry Collector for receiving and routing distributed traces.
- **Demo Environment:** Includes a full suite of demo microservices (`api-gateway-demo`, `user`, `order`, `payment`, `inventory`, `notification`) that generate realistic telemetry.

## Telemetry Flow
1. Demo services are instrumented with OpenTelemetry.
2. Telemetry is sent to the `otel-collector`.
3. The collector pushes data into Kafka topics.
4. `ingestion-service` consumes the Kafka streams and writes to TimescaleDB.
5. `query-service` and `alerting-service` use this data to display dashboards and trigger incidents.

## AI Capabilities
- **Anomaly Detection:** Uses Isolation Forests to identify latency and error rate spikes.
- **Predictive Analytics:** Employs Linear Regression to forecast future traffic and capacity needs.
- **Root Cause & Blast Radius Analysis:** Analyzes the dependency graph and historical traces to pinpoint the origin of an incident and map its downstream impact.

## Prerequisites
- Java 21
- Node.js (v20+ recommended)
- Python 3.10+
- Docker Desktop

## Setup and Run Instructions

1. **Environment Configuration:**
   Copy `.env.example` to `.env` and set your desired variables (e.g., database passwords).

2. **Start Infrastructure:**
   ```bash
   docker-compose up -d
   ```

3. **Start Backend & Demo Services:**
   On Windows, use the provided PowerShell script which builds the Maven projects and starts them in the background:
   ```powershell
   .\start-all.ps1
   ```

4. **Start AI Service:**
   ```bash
   cd ai-service
   python -m venv venv
   # Activate venv (e.g., .\venv\Scripts\activate on Windows)
   pip install -r requirements.txt
   uvicorn main:app --port 8005
   ```

5. **Start Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

6. **Generate Traffic:**
   ```powershell
   .\generate-traffic.ps1
   ```

## Ports Reference
- **Frontend:** 3000
- **Management API Gateway:** 8080
- **Backend Services:** 8081-8084
- **AI Service:** 8005
- **Demo API Gateway:** 9000
- **Demo Microservices:** 9001-9005

## Troubleshooting
- **Service Port Conflicts:** Ensure all ports between 8080-8084, 9000-9005, 8005, and 3000 are available.
- **Missing Telemetry:** Verify that `docker-compose` is running and the OpenTelemetry collector is active. Run `.\generate-traffic.ps1` to populate initial data.
- **Build Errors:** Run `./mvnw.cmd clean install -DskipTests` to ensure all Maven dependencies are downloaded and the project builds successfully.
