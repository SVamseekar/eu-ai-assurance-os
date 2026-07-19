package os.assurance.eu.api.audit;

import java.util.UUID;

public record AuditChainVerifyResponse(
    boolean valid,
    int checkedCount,
    UUID firstBreakId) {
}
