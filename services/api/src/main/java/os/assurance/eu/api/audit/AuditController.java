package os.assurance.eu.api.audit;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.system.AiSystemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditController {
  private final AuditService auditService;
  private final AiSystemRepository systems;

  public AuditController(AuditService auditService, AiSystemRepository systems) {
    this.auditService = auditService;
    this.systems = systems;
  }

  @GetMapping
  public List<AuditEvent> listAuditEvents(@RequestParam(required = false) UUID systemId) {
    if (systemId != null) {
      return auditService.findBySystemId(systemId);
    }
    return auditService.findAll();
  }

  @GetMapping("/verify-chain")
  public AuditChainVerifyResponse verifyChain() {
    return auditService.verifyChain();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AuditEvent appendAuditEvent(@Valid @RequestBody CreateAuditEventRequest request) {
    if (request.systemId() != null && systems.findById(request.systemId()).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found");
    }
    return auditService.append(
        request.systemId(),
        request.eventType(),
        request.resourceType(),
        request.resourceId(),
        request.payload() == null ? Map.of() : request.payload());
  }
}
