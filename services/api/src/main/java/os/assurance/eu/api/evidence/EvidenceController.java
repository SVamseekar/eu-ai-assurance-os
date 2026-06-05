package os.assurance.eu.api.evidence;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/evidence")
public class EvidenceController {
  private final AiSystemRepository systems;
  private final AuditService auditService;

  public EvidenceController(AiSystemRepository systems, AuditService auditService) {
    this.systems = systems;
    this.auditService = auditService;
  }

  @PostMapping("/query")
  public EvidenceQueryResponse queryEvidence(@Valid @RequestBody EvidenceQueryRequest request) {
    AiSystem system = systems.findById(request.systemId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
    EvidenceQueryResponse response = new EvidenceQueryResponse(
        "%s currently has a %s release decision. Open gaps: %s."
            .formatted(system.name(), system.releaseDecision(), String.join("; ", system.openGaps())),
        system.evidenceCoverage() / 100.0,
        List.of(
            new Citation(
                "doc_dpia_mvp",
                "%s DPIA".formatted(system.name()),
                "Release controls",
                "Reviewer action is required before unresolved high-risk blockers are released."),
            new Citation(
                "doc_control_map_mvp",
                "EU AI Act control map",
                "High-risk systems",
                "Risk management, data governance, logging, transparency, human oversight, accuracy, and cybersecurity controls must be evidenced.")));
    auditService.append(
        system.id(),
        "evidence.query_answered",
        "ai_system",
        system.id().toString(),
        Map.of("question", request.question(), "confidence", response.confidence()));
    return response;
  }
}
