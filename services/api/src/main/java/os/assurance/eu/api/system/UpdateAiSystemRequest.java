package os.assurance.eu.api.system;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public record UpdateAiSystemRequest(
    String name,
    String owner,
    String purpose,
    RiskClass riskClass,
    String riskBasis,
    String deploymentRegion,
    @Min(0) @Max(100) Integer evidenceCoverage,
    @Min(0) @Max(100) Integer evalScore,
    DataContractStatus dataContractStatus,
    List<String> openGaps) {
}
