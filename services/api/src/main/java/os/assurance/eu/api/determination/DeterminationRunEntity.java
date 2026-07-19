package os.assurance.eu.api.determination;

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
@Table(name = "determination_runs")
public class DeterminationRunEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Convert(converter = JsonMapConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  private Map<String, Object> questionnaireJson;

  @Convert(converter = JsonMapConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  private Map<String, Object> resultJson;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private String rulesetVersion;

  private UUID createdBy;

  @Column(nullable = false)
  private Instant createdAt;

  protected DeterminationRunEntity() {
  }

  public DeterminationRunEntity(
      UUID id,
      UUID tenantId,
      UUID systemId,
      Map<String, Object> questionnaireJson,
      Map<String, Object> resultJson,
      String status,
      String rulesetVersion,
      UUID createdBy,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.questionnaireJson = questionnaireJson;
    this.resultJson = resultJson;
    this.status = status;
    this.rulesetVersion = rulesetVersion;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public UUID systemId() {
    return systemId;
  }

  public Map<String, Object> questionnaireJson() {
    return questionnaireJson;
  }

  public Map<String, Object> resultJson() {
    return resultJson;
  }

  public String status() {
    return status;
  }

  public String rulesetVersion() {
    return rulesetVersion;
  }

  public UUID createdBy() {
    return createdBy;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
