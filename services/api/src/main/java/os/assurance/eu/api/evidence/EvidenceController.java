package os.assurance.eu.api.evidence;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/evidence")
public class EvidenceController {
  private final AiSystemRepository systems;
  private final EvidenceService evidenceService;

  public EvidenceController(AiSystemRepository systems, EvidenceService evidenceService) {
    this.systems = systems;
    this.evidenceService = evidenceService;
  }

  @PostMapping("/documents")
  @ResponseStatus(HttpStatus.CREATED)
  public EvidenceDocumentResponse createDocument(@Valid @RequestBody CreateEvidenceDocumentRequest request) {
    systems.findById(request.systemId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
    return evidenceService.ingest(request);
  }

  @GetMapping("/systems/{systemId}/documents")
  public List<EvidenceDocumentResponse> listDocuments(@PathVariable UUID systemId) {
    systems.findById(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
    return evidenceService.listDocuments(systemId);
  }

  @PostMapping("/query")
  public EvidenceQueryResponse queryEvidence(@Valid @RequestBody EvidenceQueryRequest request) {
    AiSystem system = systems.findById(request.systemId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
    return evidenceService.answer(system, request.question());
  }
}
