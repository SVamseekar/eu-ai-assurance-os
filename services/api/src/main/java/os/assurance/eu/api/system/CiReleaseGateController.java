package os.assurance.eu.api.system;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.observability.AssuranceMetrics;

/**
 * CI/CD-oriented release gate endpoint. Same decision engine as
 * {@code GET /api/v1/systems/{id}/release-gate}, with a stable machine-friendly contract.
 *
 * <p>Authenticate with {@code X-Api-Key} (recommended for bots) or Bearer JWT.
 */
@RestController
@RequestMapping("/api/v1/ci")
public class CiReleaseGateController {
  private final AiSystemRepository repository;
  private final ReleaseGateService releaseGateService;
  private final AuditService auditService;
  private final AssuranceMetrics assuranceMetrics;

  public CiReleaseGateController(
      AiSystemRepository repository,
      ReleaseGateService releaseGateService,
      AuditService auditService,
      AssuranceMetrics assuranceMetrics) {
    this.repository = repository;
    this.releaseGateService = releaseGateService;
    this.auditService = auditService;
    this.assuranceMetrics = assuranceMetrics;
  }

  @GetMapping("/release-gate")
  public CiReleaseGateResponse getReleaseGate(@RequestParam UUID systemId) {
    AiSystem system = repository.findById(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
    ReleaseGateResponse gate = releaseGateService.calculate(system);
    CiReleaseGateResponse response = CiReleaseGateResponse.from(system, gate);
    assuranceMetrics.releaseGateDecision(response.decision());
    auditService.append(
        system.id(),
        "release_gate.ci_calculated",
        "ai_system",
        system.id().toString(),
        Map.of(
            "decision", response.decision(),
            "blockers", response.blockers(),
            "exitCode", response.exitCode(),
            "source", "ci"));
    return response;
  }
}
