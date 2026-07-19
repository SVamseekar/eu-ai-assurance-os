package os.assurance.eu.api.regmonitor;

import java.util.UUID;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/systems/{systemId}/reg-monitor")
public class SystemRegMonitorController {
  private final RegMonitorService service;
  private final TenantAuthorizationService authorizationService;

  public SystemRegMonitorController(
      RegMonitorService service,
      TenantAuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  @GetMapping("/relevant")
  public RegMonitorFeedResponse relevant(@PathVariable UUID systemId) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.AI_ENGINEERING_LEAD,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL,
        UserRole.AUDITOR);
    return service.listRelevantForSystem(systemId);
  }
}
