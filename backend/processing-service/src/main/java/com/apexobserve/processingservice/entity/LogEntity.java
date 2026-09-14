package com.apexobserve.processingservice.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

class LogId implements Serializable {
    private UUID id;
    private OffsetDateTime time;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogId logId = (LogId) o;
        return Objects.equals(id, logId.id) && Objects.equals(time, logId.time);
    }

    @Override
    public int hashCode() { return Objects.hash(id, time); }
}

@Entity
@Table(name = "logs")
@IdClass(LogId.class)
public class LogEntity {
    @Id private UUID id;
    @Column(name = "tenant_id") private String tenantId;
    @Column(name = "service_id") private String serviceId;
    @Column(name = "trace_id") private String traceId;
    @Column(name = "span_id") private String spanId;
    @Column(name = "severity") private String severity;
    @Column(name = "message") private String message;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "attributes", columnDefinition = "jsonb") private String attributes;
    @Id @Column(name = "time") private OffsetDateTime time;

    public void setId(UUID id) { this.id = id; }
    public void setTenantId(String t) { this.tenantId = t; }
    public void setServiceId(String s) { this.serviceId = s; }
    public void setTraceId(String t) { this.traceId = t; }
    public void setSpanId(String s) { this.spanId = s; }
    public void setSeverity(String s) { this.severity = s; }
    public void setMessage(String m) { this.message = m; }
    public void setAttributes(String a) { this.attributes = a; }
    public void setTime(OffsetDateTime t) { this.time = t; }
}
