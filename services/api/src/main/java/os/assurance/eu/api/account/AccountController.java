package os.assurance.eu.api.account;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import os.assurance.eu.api.tenant.SessionOnly;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserRole;

/** Self-service data export and deletion for workspace admins, from a signed-in session only. */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
  private final WorkspaceExportService export;
  private final WorkspaceDeletionService deletion;
  private final TenantAuthorizationService authorization;
  private final TenantContext tenantContext;

  public AccountController(WorkspaceExportService export, WorkspaceDeletionService deletion,
      TenantAuthorizationService authorization, TenantContext tenantContext) {
    this.export = export;
    this.deletion = deletion;
    this.authorization = authorization;
    this.tenantContext = tenantContext;
  }

  @GetMapping(value = "/export", produces = "application/zip")
  public ResponseEntity<byte[]> export(HttpServletRequest request) {
    SessionOnly.require(request);
    authorization.requireAnyRole(UserRole.ADMIN);
    byte[] zip = export.export(tenantContext.tenantId());
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assurance-os-export.zip\"")
        .contentType(MediaType.parseMediaType("application/zip"))
        .body(zip);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, String> delete(HttpServletRequest request, @RequestBody(required = false) DeleteBody body) {
    SessionOnly.require(request);
    authorization.requireAnyRole(UserRole.ADMIN);
    deletion.delete(body == null ? null : body.confirmOrganisationName());
    return Map.of("status", "deletion_scheduled");
  }

  public record DeleteBody(String confirmOrganisationName) {}
}
