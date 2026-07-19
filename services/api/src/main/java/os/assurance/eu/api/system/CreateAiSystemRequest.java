package os.assurance.eu.api.system;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateAiSystemRequest(
    @NotBlank String name,
    @NotBlank String owner,
    @NotBlank String purpose,
    @NotNull RiskClass riskClass,
    @NotBlank String riskBasis,
    @NotBlank String deploymentRegion,
    @Min(0) @Max(100) Integer evidenceCoverage,
    @Min(0) @Max(100) Integer evalScore,
    DataContractStatus dataContractStatus,
    List<String> openGaps,
    String vendorName,
    String modelName,
    String modelVersion,
    List<String> dataSources,
    String sector,
    String decisionImpact,
    List<String> affectedUsers) {
}
