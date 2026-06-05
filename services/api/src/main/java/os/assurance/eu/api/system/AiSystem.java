package os.assurance.eu.api.system;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiSystem(
    UUID id,
    String name,
    String owner,
    String purpose,
    RiskClass riskClass,
    String riskBasis,
    String deploymentRegion,
    int evidenceCoverage,
    int evalScore,
    DataContractStatus dataContractStatus,
    ReleaseDecision releaseDecision,
    List<String> openGaps,
    Instant createdAt,
    Instant updatedAt) {
}
