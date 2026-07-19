package os.assurance.eu.api.determination;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.persistence.StringListConverter;

@Entity
@Table(name = "determination_obligations")
public class DeterminationObligationEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID runId;

  @Column(nullable = false)
  private String ruleCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Applicability applicability;

  @Column(nullable = false)
  private String rationale;

  @Convert(converter = StringListConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  private List<String> controlCodes;

  @Column(nullable = false)
  private String legalRefs;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String severity;

  protected DeterminationObligationEntity() {
  }

  public DeterminationObligationEntity(
      UUID id,
      UUID runId,
      String ruleCode,
      Applicability applicability,
      String rationale,
      List<String> controlCodes,
      String legalRefs,
      String title,
      String severity) {
    this.id = id;
    this.runId = runId;
    this.ruleCode = ruleCode;
    this.applicability = applicability;
    this.rationale = rationale;
    this.controlCodes = controlCodes;
    this.legalRefs = legalRefs;
    this.title = title;
    this.severity = severity;
  }

  public DeterminationObligation toDomain() {
    return new DeterminationObligation(
        id, ruleCode, title, applicability, rationale, controlCodes, legalRefs, severity);
  }

  public UUID runId() {
    return runId;
  }
}
