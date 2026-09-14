-- -----------------------------------------------------------------------------
-- ApexObserve Initial Schema (PostgreSQL + TimescaleDB)
-- -----------------------------------------------------------------------------

-- Create extension for TimescaleDB
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- -----------------------------------------------------------------------------
-- Metrics Schema
-- -----------------------------------------------------------------------------
CREATE TABLE metrics (
    tenant_id VARCHAR(50) NOT NULL,
    service_id VARCHAR(100) NOT NULL,
    metric_name VARCHAR(200) NOT NULL,
    label_hash VARCHAR(64) NOT NULL,
    labels JSONB NOT NULL,
    metric_type VARCHAR(20) NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (tenant_id, service_id, metric_name, label_hash, time)
);

-- Convert metrics to a hypertable
SELECT create_hypertable('metrics', 'time', chunk_time_interval => INTERVAL '1 day', if_not_exists => TRUE);

-- Create index on labels for fast filtering
CREATE INDEX idx_metrics_labels ON metrics USING GIN (labels);

-- -----------------------------------------------------------------------------
-- Logs Schema
-- -----------------------------------------------------------------------------
CREATE TABLE logs (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    service_id VARCHAR(100) NOT NULL,
    trace_id VARCHAR(64),
    span_id VARCHAR(64),
    severity VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    attributes JSONB,
    time TIMESTAMPTZ NOT NULL
);

-- Convert logs to a hypertable
SELECT create_hypertable('logs', 'time', chunk_time_interval => INTERVAL '1 day', if_not_exists => TRUE);

CREATE INDEX idx_logs_trace_id ON logs(trace_id);
CREATE INDEX idx_logs_service ON logs(tenant_id, service_id, time DESC);
CREATE INDEX idx_logs_attributes ON logs USING GIN (attributes);

-- -----------------------------------------------------------------------------
-- Traces Schema
-- -----------------------------------------------------------------------------
CREATE TABLE traces (
    trace_id VARCHAR(64) NOT NULL,
    span_id VARCHAR(64) NOT NULL,
    parent_span_id VARCHAR(64),
    tenant_id VARCHAR(50) NOT NULL,
    service_id VARCHAR(100) NOT NULL,
    operation_name VARCHAR(200) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    duration_ms BIGINT NOT NULL,
    status_code VARCHAR(20) NOT NULL,
    attributes JSONB,
    PRIMARY KEY (tenant_id, trace_id, span_id)
);

-- Time-based hypertable based on start_time
SELECT create_hypertable('traces', 'start_time', chunk_time_interval => INTERVAL '1 day', if_not_exists => TRUE);

CREATE INDEX idx_traces_service ON traces(tenant_id, service_id, start_time DESC);
CREATE INDEX idx_traces_operation ON traces(tenant_id, service_id, operation_name, start_time DESC);
CREATE INDEX idx_traces_attributes ON traces USING GIN (attributes);

-- -----------------------------------------------------------------------------
-- Alerts & Incidents Schema
-- -----------------------------------------------------------------------------
CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    service_id VARCHAR(100) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    context JSONB
);

CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    rca_summary TEXT,
    affected_services JSONB
);

CREATE TABLE incident_alerts (
    incident_id UUID NOT NULL REFERENCES incidents(id),
    alert_id UUID NOT NULL REFERENCES alerts(id),
    linked_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (incident_id, alert_id)
);

CREATE INDEX idx_alerts_status ON alerts(tenant_id, status);
CREATE INDEX idx_incidents_status ON incidents(tenant_id, status);
