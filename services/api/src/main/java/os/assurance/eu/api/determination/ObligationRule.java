package os.assurance.eu.api.determination;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ObligationRule(
    UUID id,
    String code,
    String title,
    String description,
    String legalRefs,
    Map<String, Object> appliesWhen,
    String severity,
    List<String> controlCodes,
    String rulesetVersion,
    boolean active) {
}
