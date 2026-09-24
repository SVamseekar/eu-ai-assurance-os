package os.assurance.eu.api.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "evidence_exceptions")
public class EvidenceExceptionEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private UUID proposalId;

  @Column(nullable = false, length = 2048)
  private String rationale;

  @Column(nullable = false)
  private LocalDate expiresOn;

  @Column(nullable = false)
  private UUID createdBy;

  @Column(nullable = false)
  private Instant createdAt;

  protected EvidenceExceptionEntity() {
  }

  public EvidenceExceptionEntity(
      UUID id,
      UUID tenantId,
      UUID systemId,
      UUID proposalId,
      String rationale,
      LocalDate expiresOn,
      UUID createdBy,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.proposalId = proposalId;
    this.rationale = rationale;
    this.expiresOn = expiresOn;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
  }

  public String rationale() {
    return rationale;
  }

  public LocalDate expiresOn() {
    return expiresOn;
  }
}
