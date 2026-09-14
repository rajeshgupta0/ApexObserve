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

class MetricId implements Serializable {
    private String tenantId;
    private String serviceId;
    private String metricName;
    private String labelHash;
    private OffsetDateTime time;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricId metricId = (MetricId) o;
        return Objects.equals(tenantId, metricId.tenantId) &&
                Objects.equals(serviceId, metricId.serviceId) &&
                Objects.equals(metricName, metricId.metricName) &&
                Objects.equals(labelHash, metricId.labelHash) &&
                Objects.equals(time, metricId.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, serviceId, metricName, labelHash, time);
    }
}

@Entity
@Table(name = "metrics")
@IdClass(MetricId.class)
public class MetricEntity {
    @Id @Column(name = "tenant_id") private String tenantId;
    @Id @Column(name = "service_id") private String serviceId;
    @Id @Column(name = "metric_name") private String metricName;
    @Id @Column(name = "label_hash") private String labelHash;
    @Id @Column(name = "time") private OffsetDateTime time;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "labels", columnDefinition = "jsonb") private String labels;
    @Column(name = "metric_type") private String metricType;
    @Column(name = "value") private Double value;

    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public String getServiceId() { return serviceId; }
    public String getMetricName() { return metricName; }
    public String getLabelHash() { return labelHash; }
    public OffsetDateTime getTime() { return time; }
    public String getLabels() { return labels; }
    public String getMetricType() { return metricType; }
    public Double getValue() { return value; }
}
