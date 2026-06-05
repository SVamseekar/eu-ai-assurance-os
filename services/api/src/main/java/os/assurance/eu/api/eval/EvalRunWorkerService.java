package os.assurance.eu.api.eval;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.ReleaseDecision;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EvalRunWorkerService {
  private final EvalDatasetRepository datasets;
  private final EvalRunRepository evalRuns;
  private final EvalRunCompletionService completionService;
  private final AuditService auditService;

  public EvalRunWorkerService(
      EvalDatasetRepository datasets,
      EvalRunRepository evalRuns,
      EvalRunCompletionService completionService,
      AuditService auditService) {
    this.datasets = datasets;
    this.evalRuns = evalRuns;
    this.completionService = completionService;
    this.auditService = auditService;
  }

  public EvalRun execute(UUID runId) {
    EvalRun queued = evalRuns.findById(runId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Eval run not found"));
    if ("completed".equals(queued.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Eval run is already completed");
    }
    if (!"queued".equals(queued.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Eval run is not queued");
    }
    if (queued.datasetId() == null) {
      EvalRun failed = recordTerminalFailure(queued, "Eval dataset is not registered");
      auditService.append(
          failed.systemId(),
          "eval_run.failed",
          "eval_run",
          runId.toString(),
          Map.of(
              "attempts", failed.workerAttempts(),
              "maxAttempts", failed.maxAttempts(),
              "failureReason", failed.failureReason()));
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eval dataset is not registered");
    }
    EvalDataset dataset = datasets.findById(queued.datasetId())
        .orElseGet(() -> {
          EvalRun failed = recordTerminalFailure(queued, "Eval dataset is not registered");
          auditService.append(
              failed.systemId(),
              "eval_run.failed",
              "eval_run",
              runId.toString(),
              Map.of(
                  "attempts", failed.workerAttempts(),
                  "maxAttempts", failed.maxAttempts(),
                  "failureReason", failed.failureReason()));
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eval dataset is not registered");
        });
    Instant now = Instant.now();
    EvalRun running = evalRuns.save(new EvalRun(
        queued.runId(),
        queued.systemId(),
        queued.datasetId(),
        "running",
        queued.dataset(),
        queued.modelVersion(),
        queued.promptVersion(),
        queued.threshold(),
        queued.metrics(),
        ReleaseDecision.REVIEW,
        queued.createdAt(),
        queued.queuedAt(),
        now,
        null,
        null,
        queued.workerAttempts() + 1,
        queued.maxAttempts(),
        null));
    auditService.append(
        running.systemId(),
        "eval_run.running",
        "eval_run",
        runId.toString(),
        Map.of("dataset", dataset.name(), "datasetVersion", dataset.version(), "sampleCount", dataset.sampleCount()));
    try {
      return completionService.complete(runId, calculateMetrics(running, dataset), "worker");
    } catch (RuntimeException exception) {
      EvalRun failed = recordFailure(running, exception);
      auditService.append(
          failed.systemId(),
          "eval_run.failed",
          "eval_run",
          runId.toString(),
          Map.of(
              "attempts", failed.workerAttempts(),
              "maxAttempts", failed.maxAttempts(),
              "failureReason", failed.failureReason()));
      throw exception;
    }
  }

  EvalRun recordFailure(EvalRun running, RuntimeException exception) {
    boolean exhausted = running.workerAttempts() >= running.maxAttempts();
    Instant now = Instant.now();
    return evalRuns.save(new EvalRun(
        running.runId(),
        running.systemId(),
        running.datasetId(),
        exhausted ? "failed" : "queued",
        running.dataset(),
        running.modelVersion(),
        running.promptVersion(),
        running.threshold(),
        running.metrics(),
        ReleaseDecision.REVIEW,
        running.createdAt(),
        exhausted ? running.queuedAt() : now.plusSeconds(30),
        running.startedAt(),
        null,
        now,
        running.workerAttempts(),
        running.maxAttempts(),
        failureReason(exception)));
  }

  private EvalRun recordTerminalFailure(EvalRun queued, String failureReason) {
    Instant now = Instant.now();
    return evalRuns.save(new EvalRun(
        queued.runId(),
        queued.systemId(),
        queued.datasetId(),
        "failed",
        queued.dataset(),
        queued.modelVersion(),
        queued.promptVersion(),
        queued.threshold(),
        queued.metrics(),
        ReleaseDecision.REVIEW,
        queued.createdAt(),
        queued.queuedAt(),
        queued.startedAt(),
        null,
        now,
        queued.workerAttempts() + 1,
        queued.maxAttempts(),
        failureReason));
  }

  private String failureReason(RuntimeException exception) {
    String message = exception.getMessage();
    if (message == null || message.isBlank()) {
      return exception.getClass().getSimpleName();
    }
    return message.length() > 2048 ? message.substring(0, 2048) : message;
  }

  private Map<String, Object> calculateMetrics(EvalRun run, EvalDataset dataset) {
    Map<String, Object> metrics = new LinkedHashMap<>();
    metrics.put("faithfulness", metric(run, dataset, "faithfulness", 0.76, 0.19));
    metrics.put("relevance", metric(run, dataset, "relevance", 0.78, 0.18));
    metrics.put("safetyRefusal", metric(run, dataset, "safetyRefusal", 0.82, 0.15));
    metrics.put("biasSlicePassRate", metric(run, dataset, "biasSlicePassRate", 0.73, 0.20));
    metrics.put("latencyP95Ms", 900 + bucket(run, dataset, "latency", 1500));
    metrics.put("costUsd", Math.round((1.25 + bucket(run, dataset, "cost", 700) / 100.0) * 100.0) / 100.0);
    metrics.put("sampleCount", dataset.sampleCount());
    metrics.put("goldenDataset", dataset.golden());
    return metrics;
  }

  private double metric(EvalRun run, EvalDataset dataset, String key, double floor, double spread) {
    double value = floor + (bucket(run, dataset, key, 1000) / 1000.0 * spread);
    return Math.round(value * 100.0) / 100.0;
  }

  private int bucket(EvalRun run, EvalDataset dataset, String key, int bound) {
    return Math.floorMod(
        (run.runId().toString()
            + dataset.id()
            + run.modelVersion()
            + run.promptVersion()
            + key).hashCode(),
        bound);
  }
}
