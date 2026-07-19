package os.assurance.eu.api.readiness;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/systems/{systemId}/certification-readiness")
public class CertificationReadinessController {
  private final CertificationReadinessService service;
  private final TenantAuthorizationService authorizationService;
  private final ObjectMapper objectMapper;

  public CertificationReadinessController(
      CertificationReadinessService service,
      TenantAuthorizationService authorizationService,
      ObjectMapper objectMapper) {
    this.service = service;
    this.authorizationService = authorizationService;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public CertificationReadinessResponse get(@PathVariable UUID systemId) {
    requireReadRole();
    return service.assessAndAudit(systemId);
  }

  @PostMapping("/export")
  public ResponseEntity<?> export(
      @PathVariable UUID systemId,
      @Valid @RequestBody(required = false) CertificationReadinessExportRequest request) {
    requireExportRole();
    String format = request == null ? "json" : request.normalisedFormat();
    CertificationReadinessResponse report = service.exportAndAudit(systemId, format);

    if ("pdf".equals(format)) {
      byte[] pdf = service.renderPdf(report);
      String filename = service.pdfFilename(report);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .header("X-Readiness-Score", String.valueOf(report.score()))
          .header("X-Readiness-Status", report.readinessStatus().name())
          .contentType(MediaType.APPLICATION_PDF)
          .body(pdf);
    }

    try {
      byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(report);
      String filename = "certification-readiness-" + systemId + ".json";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .header("X-Readiness-Score", String.valueOf(report.score()))
          .header("X-Readiness-Status", report.readinessStatus().name())
          .contentType(MediaType.APPLICATION_JSON)
          .body(json);
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(MediaType.APPLICATION_JSON);
    }
  }

  private void requireReadRole() {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.AI_ENGINEERING_LEAD,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL,
        UserRole.AUDITOR);
  }

  private void requireExportRole() {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.AI_ENGINEERING_LEAD,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL,
        UserRole.AUDITOR);
  }
}
