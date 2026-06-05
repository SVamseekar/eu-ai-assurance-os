package os.assurance.eu.api.system;

import java.util.List;
import java.util.UUID;

public record ReleaseGateResponse(UUID systemId, ReleaseDecision decision, List<String> blockers) {
}
