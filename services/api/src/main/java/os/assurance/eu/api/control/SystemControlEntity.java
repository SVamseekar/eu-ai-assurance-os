package os.assurance.eu.api.control;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_controls")
public class SystemControlEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private UUID controlId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ControlStatus status;

  @Column(nullable = false)
  private boolean evidenceRequired;

  private UUID reviewerId;
  private String notes;

  @Column(nullable = false)
  private Instant updatedAt;

  protected SystemControlEntity() {}

  public SystemControlEntity(
      UUID tenantId,
      UUID id,
      UUID systemId,
      UUID controlId,
      ControlStatus status,
      boolean evidenceRequired,
      UUID reviewerId,
      String notes,
      Instant updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.controlId = controlId;
    this.status = status;
    this.evidenceRequired = evidenceRequired;
    this.reviewerId = reviewerId;
    this.notes = notes;
    this.updatedAt = updatedAt;
  }

  public UUID id() { return id; }
  public UUID systemId() { return systemId; }
  public UUID controlId() { return controlId; }
  public ControlStatus status() { return status; }
  public boolean evidenceRequired() { return evidenceRequired; }
  public UUID reviewerId() { return reviewerId; }
  public String notes() { return notes; }
  public Instant updatedAt() { return updatedAt; }
}
