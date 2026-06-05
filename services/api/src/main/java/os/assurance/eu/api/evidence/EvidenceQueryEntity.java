package os.assurance.eu.api.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evidence_queries")
public class EvidenceQueryEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private String question;

  @Column(nullable = false)
  private String answer;

  @Column(nullable = false)
  private double confidence;

  @Convert(converter = CitationListConverter.class)
  @Column(name = "citations_json", nullable = false)
  private List<Citation> citations;

  @Column(nullable = false)
  private UUID createdBy;

  @Column(nullable = false)
  private Instant createdAt;

  protected EvidenceQueryEntity() {
  }

  public EvidenceQueryEntity(
      UUID id,
      UUID tenantId,
      UUID systemId,
      String question,
      String answer,
      double confidence,
      List<Citation> citations,
      UUID createdBy,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.question = question;
    this.answer = answer;
    this.confidence = confidence;
    this.citations = citations;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
  }
}
