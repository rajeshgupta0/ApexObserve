package com.apexobserve.alertingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "alerts")
public class AlertEntity {
    @Id private UUID id;
    @Column(name = "tenant_id") private String tenantId;
    @Column(name = "service_id") private String serviceId;
    @Column(name = "rule_name") private String ruleName;
    private String severity;
    private String status;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @Column(name = "resolved_at") private OffsetDateTime resolvedAt;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "context", columnDefinition = "jsonb") private String context;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
