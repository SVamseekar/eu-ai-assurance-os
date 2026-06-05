package os.assurance.eu.api.eval;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.ReleaseDecision;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EvalRunOperationsService {
  private final EvalRunRepository evalRuns;
  private final AuditService auditService;
  private final EvalRunMetrics metrics;

  public EvalRunOperationsService(EvalRunRepository evalRuns, AuditService auditService, EvalRunMetrics metrics) {
    this.evalRuns = evalRuns;
    this.auditService = auditService;
    this.metrics = metrics;
  }

  public EvalRunOperationsView operationsView() {
    return new EvalRunOperationsView(
        evalRuns.countByStatus("queued"),
        evalRuns.countByStatus("running"),
        evalRuns.countByStatus("failed"),
        evalRuns.findQueuedRetryable(),
        evalRuns.findDeadLetter());
  }

  @Transactional
  public EvalRun retryFailed(UUID runId) {
    EvalRun failed = evalRuns.findByIdForUpdate(runId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Eval run not found"));
    if (!"failed".equals(failed.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Only failed eval runs can be retried");
    }
    Instant now = Instant.now();
    EvalRun retried = evalRuns.save(new EvalRun(
        failed.runId(),
        failed.systemId(),
        failed.datasetId(),
        "queued",
        failed.dataset(),
        failed.modelVersion(),
        failed.promptVersion(),
        failed.threshold(),
        Map.of(),
        ReleaseDecision.REVIEW,
        failed.createdAt(),
        now,
        null,
        null,
        null,
        0,
        failed.maxAttempts(),
        null));
    auditService.append(
        retried.systemId(),
        "eval_run.retry_queued",
        "eval_run",
        retried.runId().toString(),
        Map.of("previousFailureReason", failed.failureReason() == null ? "" : failed.failureReason()));
    metrics.retried();
    metrics.queued("retry");
    return retried;
  }
}
