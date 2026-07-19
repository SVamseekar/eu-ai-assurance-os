package os.assurance.eu.api.determination;

import java.util.List;
import java.util.UUID;

public record DeterminationObligation(
    UUID id,
    String ruleCode,
    String title,
    Applicability applicability,
    String rationale,
    List<String> controlCodes,
    String legalRefs,
    String severity) {
}
