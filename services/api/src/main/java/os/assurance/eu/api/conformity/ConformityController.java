package os.assurance.eu.api.conformity;

import jakarta.validation.Valid;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/systems/{systemId}/conformity")
public class ConformityController {
  private final ConformityService conformityService;
  private final TenantAuthorizationService authorizationService;

  public ConformityController(
      ConformityService conformityService,
      TenantAuthorizationService authorizationService) {
    this.conformityService = conformityService;
    this.authorizationService = authorizationService;
  }

  @GetMapping
  public ConformityDossier get(@PathVariable UUID systemId) {
    return conformityService.getOrCreate(systemId);
  }

  @PutMapping
  public ConformityDossier update(
      @PathVariable UUID systemId,
      @Valid @RequestBody UpdateConformityDossierRequest request) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN, UserRole.COMPLIANCE_OFFICER, UserRole.LEGAL_COUNSEL);
    return conformityService.update(systemId, request);
  }
}
