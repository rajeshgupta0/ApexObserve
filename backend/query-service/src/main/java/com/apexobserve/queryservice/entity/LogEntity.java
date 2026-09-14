package com.apexobserve.queryservice.entity;

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

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getServiceId() { return serviceId; }
    public String getTraceId() { return traceId; }
    public String getSpanId() { return spanId; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getAttributes() { return attributes; }
    public OffsetDateTime getTime() { return time; }
}
