package os.assurance.eu.api.system;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditEvent;

/**
 * Sealed evidence pack. JSON is the primary machine-readable format (PRD MVP).
 * Seal fields (contentSha256, evidencePackVersion, generator, auditChainHead)
 * make exports deterministic and traceable under a fixed clock.
 */
public record EvidencePackResponse(
    UUID systemId,
    Instant generatedAt,
    ReleaseDecision decision,
    Map<String, Object> riskClassification,
    List<Map<String, Object>> evidence,
    List<Map<String, Object>> evalRuns,
    List<Map<String, Object>> dataContracts,
    List<Map<String, Object>> approvals,
    List<AuditEvent> auditEvents,
    String evidencePackVersion,
    String contentSha256,
    String generator,
    String auditChainHead) {
}
