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

  private UUID datasetId;

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

  @Column(nullable = false)
  private Instant queuedAt;

  private Instant startedAt;

  private Instant completedAt;

  private Instant failedAt;

  @Column(nullable = false)
  private int workerAttempts;

  @Column(nullable = false)
  private int maxAttempts;

  @Column(length = 2048)
  private String failureReason;

  protected EvalRunEntity() {
  }

  public EvalRunEntity(UUID tenantId, EvalRun evalRun) {
    this.id = evalRun.runId();
    this.tenantId = tenantId;
    this.systemId = evalRun.systemId();
    this.datasetId = evalRun.datasetId();
    this.status = evalRun.status();
    this.dataset = evalRun.dataset();
    this.modelVersion = evalRun.modelVersion();
    this.promptVersion = evalRun.promptVersion();
    this.threshold = evalRun.threshold();
    this.metrics = evalRun.metrics();
    this.releaseDecision = evalRun.releaseDecision();
    this.createdAt = evalRun.createdAt();
    this.queuedAt = evalRun.queuedAt();
    this.startedAt = evalRun.startedAt();
    this.completedAt = evalRun.completedAt();
    this.failedAt = evalRun.failedAt();
    this.workerAttempts = evalRun.workerAttempts();
    this.maxAttempts = evalRun.maxAttempts();
    this.failureReason = evalRun.failureReason();
  }

  public EvalRun toDomain() {
    return new EvalRun(
        id,
        systemId,
        datasetId,
        status,
        dataset,
        modelVersion,
        promptVersion,
        threshold,
        metrics,
        releaseDecision,
        createdAt,
        queuedAt,
        startedAt,
        completedAt,
        failedAt,
        workerAttempts,
        maxAttempts,
        failureReason);
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }
}
