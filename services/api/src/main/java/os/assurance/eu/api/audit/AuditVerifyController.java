package os.assurance.eu.api.audit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditVerifyController {
  private final AuditService auditService;

  public AuditVerifyController(AuditService auditService) {
    this.auditService = auditService;
  }

  @GetMapping("/verify")
  public AuditChainVerifyResponse verify() {
    return auditService.verifyChain();
  }
}
