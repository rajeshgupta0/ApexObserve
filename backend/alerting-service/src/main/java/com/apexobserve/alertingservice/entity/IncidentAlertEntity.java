package com.apexobserve.alertingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

class IncidentAlertId implements Serializable {
    private UUID incidentId;
    private UUID alertId;
    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentAlertId that = (IncidentAlertId) o;
        return Objects.equals(incidentId, that.incidentId) && Objects.equals(alertId, that.alertId);
    }
    @Override public int hashCode() { return Objects.hash(incidentId, alertId); }
}

@Entity
@Table(name = "incident_alerts")
@IdClass(IncidentAlertId.class)
public class IncidentAlertEntity {
    @Id @Column(name = "incident_id") private UUID incidentId;
    @Id @Column(name = "alert_id") private UUID alertId;
    @Column(name = "linked_at") private OffsetDateTime linkedAt;

    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }
    public UUID getAlertId() { return alertId; }
    public void setAlertId(UUID alertId) { this.alertId = alertId; }
    public OffsetDateTime getLinkedAt() { return linkedAt; }
    public void setLinkedAt(OffsetDateTime linkedAt) { this.linkedAt = linkedAt; }
}
