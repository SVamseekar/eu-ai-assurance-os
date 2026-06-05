package os.assurance.eu.api.contract;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data-contracts")
public class DataContractController {
  private final AuditService auditService;

  public DataContractController(AuditService auditService) {
    this.auditService = auditService;
  }

  @PostMapping("/{contractId}/drift-events")
  @ResponseStatus(HttpStatus.CREATED)
  public DriftEventResponse createDriftEvent(
      @PathVariable UUID contractId,
      @Valid @RequestBody DriftEventRequest request) {
    UUID eventId = UUID.randomUUID();
    Instant createdAt = Instant.now();
    auditService.append(
        null,
        "data_contract.drift_detected",
        "drift_event",
        eventId.toString(),
        Map.of(
            "contractId", contractId,
            "severity", request.severity(),
            "field", request.field() == null ? "" : request.field()));
    return new DriftEventResponse(eventId, contractId, request.severity(), "open", createdAt);
  }
}
