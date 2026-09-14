package com.apexobserve.processingservice.entity;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraceId traceIdObj = (TraceId) o;
        return Objects.equals(tenantId, traceIdObj.tenantId) &&
                Objects.equals(traceId, traceIdObj.traceId) &&
                Objects.equals(spanId, traceIdObj.spanId);
    }
    @Override
    public int hashCode() { return Objects.hash(tenantId, traceId, spanId); }
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

    // Getters/Setters
    public void setTenantId(String t) { this.tenantId = t; }
    public void setTraceId(String t) { this.traceId = t; }
    public void setSpanId(String s) { this.spanId = s; }
    public void setParentSpanId(String p) { this.parentSpanId = p; }
    public void setServiceId(String s) { this.serviceId = s; }
    public void setOperationName(String o) { this.operationName = o; }
    public void setStartTime(OffsetDateTime s) { this.startTime = s; }
    public void setEndTime(OffsetDateTime e) { this.endTime = e; }
    public void setDurationMs(Long d) { this.durationMs = d; }
    public void setStatusCode(String s) { this.statusCode = s; }
    public void setAttributes(String a) { this.attributes = a; }
}
