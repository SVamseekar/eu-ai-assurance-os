package os.assurance.eu.api.determination;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DeterminationController {
  private final DeterminationService determinationService;
  private final TenantAuthorizationService authorizationService;

  public DeterminationController(
      DeterminationService determinationService,
      TenantAuthorizationService authorizationService) {
    this.determinationService = determinationService;
    this.authorizationService = authorizationService;
  }

  @GetMapping("/determination/questionnaire")
  public QuestionnaireDefinition questionnaire() {
    return determinationService.questionnaire();
  }

  @PostMapping("/systems/{systemId}/determination/runs")
  public DeterminationRun createRun(
      @PathVariable UUID systemId,
      @Valid @RequestBody CreateDeterminationRunRequest request) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL,
        UserRole.AI_ENGINEERING_LEAD);
    return determinationService.createRun(systemId, request.answers());
  }

  @GetMapping("/systems/{systemId}/determination/runs")
  public List<DeterminationRun> listRuns(@PathVariable UUID systemId) {
    return determinationService.listRuns(systemId);
  }

  @GetMapping("/systems/{systemId}/determination/runs/{runId}")
  public DeterminationRun getRun(
      @PathVariable UUID systemId,
      @PathVariable UUID runId) {
    return determinationService.getRun(systemId, runId);
  }
}
