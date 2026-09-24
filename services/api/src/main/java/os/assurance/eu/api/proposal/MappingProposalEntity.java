package os.assurance.eu.api.proposal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mapping_proposals")
public class MappingProposalEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private String relation;

  @Column(nullable = false)
  private String corpusVersion;

  private String adapterVersion;
  private String provisionKey;

  @Column(length = 4000)
  private String excerpt;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant decidedAt;
  private UUID decidedBy;
  private String controlMode;
  private Instant reopenedAt;

  protected MappingProposalEntity() {
  }

  public MappingProposalEntity(
      UUID id,
      UUID tenantId,
      UUID systemId,
      String status,
      String relation,
      String corpusVersion,
      String adapterVersion,
      String provisionKey,
      String excerpt,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.status = status;
    this.relation = relation;
    this.corpusVersion = corpusVersion;
    this.adapterVersion = adapterVersion;
    this.provisionKey = provisionKey;
    this.excerpt = excerpt;
    this.createdAt = createdAt;
  }

  public UUID id() {
    return id;
  }

  public UUID systemId() {
    return systemId;
  }

  public String status() {
    return status;
  }

  public String relation() {
    return relation;
  }

  public String corpusVersion() {
    return corpusVersion;
  }

  public String adapterVersion() {
    return adapterVersion;
  }

  public String provisionKey() {
    return provisionKey;
  }

  public String excerpt() {
    return excerpt;
  }

  public String controlMode() {
    return controlMode;
  }

  public Instant reopenedAt() {
    return reopenedAt;
  }

  public Instant decidedAt() {
    return decidedAt;
  }

  public void decide(String status, UUID actorId, Instant decidedAt) {
    this.status = status;
    this.decidedBy = actorId;
    this.decidedAt = decidedAt;
  }

  public void assignMode(String controlMode) {
    this.controlMode = controlMode;
  }

  public void reopen(Instant reopenedAt) {
    this.reopenedAt = reopenedAt;
  }
}
