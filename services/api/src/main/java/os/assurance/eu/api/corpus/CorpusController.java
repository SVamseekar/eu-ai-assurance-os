package os.assurance.eu.api.corpus;

import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/corpus")
public class CorpusController {
  private final CorpusQueryService corpus;
  private final TenantAuthorizationService authorizationService;

  public CorpusController(CorpusQueryService corpus, TenantAuthorizationService authorizationService) {
    this.corpus = corpus;
    this.authorizationService = authorizationService;
  }

  @GetMapping
  public CorpusQueryService.CorpusView current() {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.AI_ENGINEERING_LEAD,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL,
        UserRole.AUDITOR);
    return corpus.current();
  }
}
