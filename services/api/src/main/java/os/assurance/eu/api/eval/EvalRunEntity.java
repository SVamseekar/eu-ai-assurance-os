package os.assurance.eu.api.eval;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.persistence.JsonMapConverter;
import os.assurance.eu.api.system.ReleaseDecision;

@Entity
@Table(name = "eval_runs")
public class EvalRunEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private String dataset;

  @Column(nullable = false)
  private String modelVersion;

  @Column(nullable = false)
  private String promptVersion;

  @Column(nullable = false)
  private double threshold;

  @Convert(converter = JsonMapConverter.class)
  @Column(name = "metrics_json", nullable = false)
  private Map<String, Object> metrics;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReleaseDecision releaseDecision;

  @Column(nullable = false)
  private Instant createdAt;

  protected EvalRunEntity() {
  }

  public EvalRunEntity(UUID tenantId, EvalRun evalRun) {
    this.id = evalRun.runId();
    this.tenantId = tenantId;
    this.systemId = evalRun.systemId();
    this.status = evalRun.status();
    this.dataset = evalRun.dataset();
    this.modelVersion = evalRun.modelVersion();
    this.promptVersion = evalRun.promptVersion();
    this.threshold = evalRun.threshold();
    this.metrics = evalRun.metrics();
    this.releaseDecision = evalRun.releaseDecision();
    this.createdAt = evalRun.createdAt();
  }

  public EvalRun toDomain() {
    return new EvalRun(
        id,
        systemId,
        status,
        dataset,
        modelVersion,
        promptVersion,
        threshold,
        metrics,
        releaseDecision,
        createdAt);
  }
}
