-- ApexObserve Database Schema
-- PostgreSQL / TimescaleDB

-- Enable TimescaleDB extension
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- ============================================================
-- Metrics table (hypertable for time-series)
-- ============================================================
CREATE TABLE IF NOT EXISTS metrics (
    tenant_id   VARCHAR(100)  NOT NULL,
    service_id  VARCHAR(200)  NOT NULL,
    metric_name VARCHAR(300)  NOT NULL,
    label_hash  VARCHAR(64)   NOT NULL,
    time        TIMESTAMPTZ   NOT NULL,
    labels      JSONB,
    metric_type VARCHAR(50),
    value       DOUBLE PRECISION,
    PRIMARY KEY (tenant_id, service_id, metric_name, label_hash, time)
);

SELECT create_hypertable('metrics', 'time', if_not_exists => TRUE);

CREATE INDEX IF NOT EXISTS idx_metrics_tenant_service ON metrics (tenant_id, service_id, time DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_name ON metrics (metric_name);

-- ============================================================
-- Logs table (hypertable for time-series)
-- ============================================================
CREATE TABLE IF NOT EXISTS logs (
    id         UUID          NOT NULL DEFAULT gen_random_uuid(),
    tenant_id  VARCHAR(100)  NOT NULL,
    service_id VARCHAR(200),
    trace_id   VARCHAR(100),
    span_id    VARCHAR(100),
    severity   VARCHAR(50),
    message    TEXT,
    attributes JSONB,
    time       TIMESTAMPTZ   NOT NULL,
    PRIMARY KEY (id, time)
);

SELECT create_hypertable('logs', 'time', if_not_exists => TRUE);

CREATE INDEX IF NOT EXISTS idx_logs_tenant_time ON logs (tenant_id, time DESC);
CREATE INDEX IF NOT EXISTS idx_logs_service ON logs (service_id);
CREATE INDEX IF NOT EXISTS idx_logs_trace ON logs (trace_id);

-- ============================================================
-- Traces table (hypertable for time-series)
-- ============================================================
CREATE TABLE IF NOT EXISTS traces (
    tenant_id       VARCHAR(100) NOT NULL,
    trace_id        VARCHAR(100) NOT NULL,
    span_id         VARCHAR(100) NOT NULL,
    parent_span_id  VARCHAR(100),
    service_id      VARCHAR(200),
    operation_name  VARCHAR(500),
    start_time      TIMESTAMPTZ,
    end_time        TIMESTAMPTZ,
    duration_ms     BIGINT,
    status_code     VARCHAR(50),
    attributes      JSONB,
    PRIMARY KEY (tenant_id, trace_id, span_id)
);

CREATE INDEX IF NOT EXISTS idx_traces_trace_id ON traces (trace_id);
CREATE INDEX IF NOT EXISTS idx_traces_service  ON traces (service_id);
CREATE INDEX IF NOT EXISTS idx_traces_time     ON traces (start_time DESC);

-- ============================================================
-- Alerting service tables
-- ============================================================
CREATE TABLE IF NOT EXISTS alerts (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100)  NOT NULL,
    service_id  VARCHAR(200),
    rule_name   VARCHAR(300),
    severity    VARCHAR(50),
    status      VARCHAR(50)   DEFAULT 'ACTIVE',
    context     JSONB,
    created_at  TIMESTAMPTZ   DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_alerts_tenant_status ON alerts (tenant_id, status);

CREATE TABLE IF NOT EXISTS incidents (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(100) NOT NULL,
    title             TEXT,
    severity          VARCHAR(50),
    status            VARCHAR(50)  DEFAULT 'OPEN',
    affected_services JSONB,
    rca_summary       TEXT,
    created_at        TIMESTAMPTZ  DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  DEFAULT NOW(),
    resolved_at       TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_incidents_tenant ON incidents (tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS incident_alerts (
    incident_id UUID REFERENCES incidents(id) ON DELETE CASCADE,
    alert_id    UUID REFERENCES alerts(id)    ON DELETE CASCADE,
    linked_at   TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (incident_id, alert_id)
);

CREATE TABLE IF NOT EXISTS evidence (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    incident_id     UUID    REFERENCES incidents(id) ON DELETE CASCADE,
    evidence_type   VARCHAR(50)  NOT NULL,  -- METRIC, LOG, TRACE, ANOMALY, DEPENDENCY
    source_id       VARCHAR(500) NOT NULL,  -- ID or name of source
    description     TEXT,
    relevance_score DOUBLE PRECISION DEFAULT 0.0,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_evidence_incident ON evidence (incident_id);
CREATE INDEX IF NOT EXISTS idx_evidence_relevance ON evidence (incident_id, relevance_score DESC);
