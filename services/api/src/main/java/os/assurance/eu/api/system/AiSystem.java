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
    String vendorName,
    String modelName,
    String modelVersion,
    List<String> dataSources,
    String sector,
    String decisionImpact,
    List<String> affectedUsers,
    Instant createdAt,
    Instant updatedAt) {

  public AiSystem {
    openGaps = openGaps == null ? List.of() : List.copyOf(openGaps);
    dataSources = dataSources == null ? List.of() : List.copyOf(dataSources);
    affectedUsers = affectedUsers == null ? List.of() : List.copyOf(affectedUsers);
  }

  public AiSystem withReleaseDecision(ReleaseDecision decision) {
    return new AiSystem(
        id, name, owner, purpose, riskClass, riskBasis, deploymentRegion,
        evidenceCoverage, evalScore, dataContractStatus, decision, openGaps,
        vendorName, modelName, modelVersion, dataSources, sector, decisionImpact,
        affectedUsers, createdAt, Instant.now());
  }

  public AiSystem withDataContractStatus(DataContractStatus status) {
    return new AiSystem(
        id, name, owner, purpose, riskClass, riskBasis, deploymentRegion,
        evidenceCoverage, evalScore, status, releaseDecision, openGaps,
        vendorName, modelName, modelVersion, dataSources, sector, decisionImpact,
        affectedUsers, createdAt, Instant.now());
  }

  public AiSystem withEvalScore(int newEvalScore) {
    return new AiSystem(
        id, name, owner, purpose, riskClass, riskBasis, deploymentRegion,
        evidenceCoverage, newEvalScore, dataContractStatus, releaseDecision, openGaps,
        vendorName, modelName, modelVersion, dataSources, sector, decisionImpact,
        affectedUsers, createdAt, Instant.now());
  }
}
