package os.assurance.eu.api.regmonitor;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reg-monitor")
public class RegMonitorController {
  private final RegMonitorService service;
  private final TenantAuthorizationService authorizationService;

  public RegMonitorController(
      RegMonitorService service,
      TenantAuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  @GetMapping("/items")
  public RegMonitorFeedResponse listItems(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
      @RequestParam(required = false) Boolean reviewed) {
    requireReadRole();
    return service.listItems(since, reviewed);
  }

  @PostMapping("/items/{itemId}/review")
  public RegItemResponse markReviewed(
      @PathVariable UUID itemId,
      @Valid @RequestBody(required = false) MarkRegItemReviewedRequest request) {
    requireReviewRole();
    String notes = request == null ? null : request.notes();
    return service.markReviewed(itemId, notes);
  }

  private void requireReadRole() {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.AI_ENGINEERING_LEAD,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL,
        UserRole.AUDITOR);
  }

  private void requireReviewRole() {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL);
  }
}
