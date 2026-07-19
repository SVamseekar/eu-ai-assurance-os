package os.assurance.eu.api.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.persistence.JsonMapConverter;

@Entity
@Table(name = "audit_events")
public class AuditEventEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  private UUID systemId;

  @Column(nullable = false)
  private UUID actorId;

  @Column(nullable = false)
  private String eventType;

  @Column(nullable = false)
  private String resourceType;

  private String resourceId;

  @Convert(converter = JsonMapConverter.class)
  @Column(name = "payload_json", nullable = false)
  private Map<String, Object> payload;

  @Column(nullable = false)
  private Instant createdAt;

  private String prevEventHash;
  private String eventHash;
  private Instant retainUntil;

  protected AuditEventEntity() {
  }

  public AuditEventEntity(
      UUID id,
      UUID tenantId,
      UUID systemId,
      UUID actorId,
      String eventType,
      String resourceType,
      String resourceId,
      Map<String, Object> payload,
      Instant createdAt,
      String prevEventHash,
      String eventHash,
      Instant retainUntil) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.actorId = actorId;
    this.eventType = eventType;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.payload = payload;
    this.createdAt = createdAt;
    this.prevEventHash = prevEventHash;
    this.eventHash = eventHash;
    this.retainUntil = retainUntil;
  }

  public UUID id() { return id; }
  public UUID tenantId() { return tenantId; }
  public UUID systemId() { return systemId; }
  public UUID actorId() { return actorId; }
  public String eventType() { return eventType; }
  public String resourceType() { return resourceType; }
  public String resourceId() { return resourceId; }
  public Map<String, Object> payload() { return payload; }
  public Instant createdAt() { return createdAt; }
  public String prevEventHash() { return prevEventHash; }
  public String eventHash() { return eventHash; }
  public Instant retainUntil() { return retainUntil; }

  public AuditEvent toDomain() {
    return new AuditEvent(
        id, systemId, actorId, eventType, resourceType, resourceId, payload, createdAt,
        prevEventHash, eventHash, retainUntil);
  }
}
