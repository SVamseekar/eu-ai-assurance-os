package os.assurance.eu.api.assessment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "assessment_applicability")
public class AssessmentApplicabilityEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private UUID proposalId;

  @Column(nullable = false)
  private String applicability;

  private UUID reviewerId;

  protected AssessmentApplicabilityEntity() {
  }

  public AssessmentApplicabilityEntity(
      UUID id,
      UUID tenantId,
      UUID systemId,
      UUID proposalId,
      String applicability,
      UUID reviewerId) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.proposalId = proposalId;
    this.applicability = applicability;
    this.reviewerId = reviewerId;
  }

  public String applicability() {
    return applicability;
  }

  public UUID reviewerId() {
    return reviewerId;
  }

  public void update(String applicability, UUID reviewerId) {
    this.applicability = applicability;
    this.reviewerId = reviewerId;
  }
}
