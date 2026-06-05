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
  private final EvalRunRepository evalRuns;
  private final AuditService auditService;

  public EvalRunController(
      AiSystemRepository systems,
      EvalRunRepository evalRuns,
      AuditService auditService) {
    this.systems = systems;
    this.evalRuns = evalRuns;
    this.auditService = auditService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public EvalRunResponse createEvalRun(@Valid @RequestBody CreateEvalRunRequest request) {
    if (systems.findById(request.systemId()).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found");
    }
    UUID runId = UUID.randomUUID();
    EvalRun evalRun = evalRuns.save(new EvalRun(
        runId,
        request.systemId(),
        "queued",
        request.dataset(),
        request.modelVersion(),
        request.promptVersion(),
        request.threshold(),
        Map.of(),
        ReleaseDecision.REVIEW,
        Instant.now()));
    auditService.append(
        request.systemId(),
        "eval_run.queued",
        "eval_run",
        runId.toString(),
        Map.of("dataset", request.dataset(), "threshold", request.threshold()));
    return new EvalRunResponse(evalRun.runId(), evalRun.status());
  }

  @GetMapping("/{runId}")
  public EvalRun getEvalRun(@PathVariable UUID runId) {
    return evalRuns.findById(runId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Eval run not found"));
  }
}
