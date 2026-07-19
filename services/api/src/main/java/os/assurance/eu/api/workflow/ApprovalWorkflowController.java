package os.assurance.eu.api.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ApprovalWorkflowController {
  private final ApprovalWorkflowService service;

  public ApprovalWorkflowController(ApprovalWorkflowService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/workflows/open")
  public List<ApprovalWorkflow> listOpenWorkflows() {
    return service.listOpen();
  }

  @GetMapping("/api/v1/workflows/mine")
  public List<ApprovalWorkflow> listMyWorkflows() {
    return service.listMine();
  }

  @GetMapping("/api/v1/workflow-notifications/mine")
  public List<WorkflowNotification> listMyNotifications() {
    return service.listMyNotifications();
  }

  @GetMapping("/api/v1/systems/{systemId}/workflows")
  public List<ApprovalWorkflow> listWorkflows(@PathVariable UUID systemId) {
    return service.listBySystemId(systemId);
  }

  @GetMapping("/api/v1/systems/{systemId}/workflows/active")
  public ApprovalWorkflow getActiveWorkflow(@PathVariable UUID systemId) {
    return service.findActive(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No active workflow for system"));
  }

  @PostMapping("/api/v1/systems/{systemId}/workflows/{workflowId}/stages/{stageId}/approve")
  public ApprovalWorkflow approveStage(
      @PathVariable UUID systemId,
      @PathVariable UUID workflowId,
      @PathVariable UUID stageId,
      @RequestBody(required = false) StageActionRequest request) {
    String rationale = request != null ? request.rationale() : null;
    String oversightEvidence = request != null ? request.oversightEvidence() : null;
    return service.approveStage(workflowId, stageId, rationale, oversightEvidence);
  }

  @PostMapping("/api/v1/systems/{systemId}/workflows/{workflowId}/stages/{stageId}/reject")
  public ApprovalWorkflow rejectStage(
      @PathVariable UUID systemId,
      @PathVariable UUID workflowId,
      @PathVariable UUID stageId,
      @RequestBody StageActionRequest request) {
    return service.rejectStage(workflowId, stageId, request.rationale());
  }

  @PostMapping("/api/v1/systems/{systemId}/workflows/{workflowId}/stages/{stageId}/override")
  public ApprovalWorkflow overrideStage(
      @PathVariable UUID systemId,
      @PathVariable UUID workflowId,
      @PathVariable UUID stageId,
      @RequestBody StageActionRequest request) {
    return service.overrideStage(workflowId, stageId, request.rationale());
  }
}
