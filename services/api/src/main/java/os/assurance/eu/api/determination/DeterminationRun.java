package os.assurance.eu.api.determination;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DeterminationRun(
    UUID id,
    UUID systemId,
    Map<String, Object> questionnaire,
    Map<String, Object> result,
    String status,
    String rulesetVersion,
    UUID createdBy,
    Instant createdAt,
    List<DeterminationObligation> obligations,
    String disclaimer) {
}
