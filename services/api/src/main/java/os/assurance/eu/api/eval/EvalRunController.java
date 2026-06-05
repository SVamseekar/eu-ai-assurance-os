package os.assurance.eu.api.eval;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.ReleaseDecision;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/eval-runs")
public class EvalRunController {
  private final AiSystemRepository systems;
  private final EvalDatasetRepository datasets;
  private final EvalRunRepository evalRuns;
  private final EvalRunCompletionService completionService;
  private final EvalRunWorkerService workerService;
  private final AuditService auditService;

  public EvalRunController(
      AiSystemRepository systems,
      EvalDatasetRepository datasets,
      EvalRunRepository evalRuns,
      EvalRunCompletionService completionService,
      EvalRunWorkerService workerService,
      AuditService auditService) {
    this.systems = systems;
    this.datasets = datasets;
    this.evalRuns = evalRuns;
    this.completionService = completionService;
    this.workerService = workerService;
    this.auditService = auditService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public EvalRunResponse createEvalRun(@Valid @RequestBody CreateEvalRunRequest request) {
    systems.findById(request.systemId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
    EvalDataset dataset = datasets.findByName(request.dataset())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eval dataset is not registered"));
    UUID runId = UUID.randomUUID();
    Instant now = Instant.now();
    EvalRun evalRun = evalRuns.save(new EvalRun(
        runId,
        request.systemId(),
        dataset.id(),
        "queued",
        dataset.name(),
        request.modelVersion(),
        request.promptVersion(),
        request.threshold(),
        Map.of(),
        ReleaseDecision.REVIEW,
        now,
        now,
        null,
        null,
        null,
        0,
        3,
        null));
    auditService.append(
        request.systemId(),
        "eval_run.queued",
        "eval_run",
        runId.toString(),
        Map.of("dataset", dataset.name(), "datasetVersion", dataset.version(), "threshold", request.threshold()));
    return new EvalRunResponse(evalRun.runId(), evalRun.status());
  }

  @GetMapping("/{runId}")
  public EvalRun getEvalRun(@PathVariable UUID runId) {
    return evalRuns.findById(runId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Eval run not found"));
  }

  @PatchMapping("/{runId}/result")
  public EvalRun completeEvalRun(
      @PathVariable UUID runId,
      @Valid @RequestBody CompleteEvalRunRequest request) {
    return completionService.complete(runId, request.metrics(), "callback");
  }

  @PostMapping("/{runId}/execute")
  public EvalRun executeEvalRun(@PathVariable UUID runId) {
    return workerService.execute(runId);
  }
}
