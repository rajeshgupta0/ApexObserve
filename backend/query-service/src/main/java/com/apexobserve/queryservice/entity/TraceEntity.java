package com.apexobserve.queryservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

class TraceId implements Serializable {
    private String tenantId;
    private String traceId;
    private String spanId;
    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraceId that = (TraceId) o;
        return Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId) && Objects.equals(spanId, that.spanId);
    }
    @Override public int hashCode() { return Objects.hash(tenantId, traceId, spanId); }
}

@Entity
@Table(name = "traces")
@IdClass(TraceId.class)
public class TraceEntity {
    @Id @Column(name = "tenant_id") private String tenantId;
    @Id @Column(name = "trace_id") private String traceId;
    @Id @Column(name = "span_id") private String spanId;
    
    @Column(name = "parent_span_id") private String parentSpanId;
    @Column(name = "service_id") private String serviceId;
    @Column(name = "operation_name") private String operationName;
    @Column(name = "start_time") private OffsetDateTime startTime;
    @Column(name = "end_time") private OffsetDateTime endTime;
    @Column(name = "duration_ms") private Long durationMs;
    @Column(name = "status_code") private String statusCode;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "attributes", columnDefinition = "jsonb") private String attributes;

    public String getTenantId() { return tenantId; }
    public String getTraceId() { return traceId; }
    public String getSpanId() { return spanId; }
    public String getParentSpanId() { return parentSpanId; }
    public String getServiceId() { return serviceId; }
    public String getOperationName() { return operationName; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public Long getDurationMs() { return durationMs; }
    public String getStatusCode() { return statusCode; }
    public String getAttributes() { return attributes; }
}
