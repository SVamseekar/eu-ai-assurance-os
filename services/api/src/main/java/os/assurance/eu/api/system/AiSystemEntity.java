package os.assurance.eu.api.system;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.persistence.StringListConverter;

@Entity
@Table(name = "ai_systems")
public class AiSystemEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String owner;

  @Column(nullable = false)
  private String purpose;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RiskClass riskClass;

  @Column(nullable = false)
  private String riskBasis;

  @Column(nullable = false)
  private String deploymentRegion;

  @Column(nullable = false)
  private int evidenceCoverage;

  @Column(nullable = false)
  private int evalScore;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DataContractStatus dataContractStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReleaseDecision releaseDecision;

  @Convert(converter = StringListConverter.class)
  @Column(nullable = false)
  private List<String> openGaps;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected AiSystemEntity() {
  }

  public AiSystemEntity(UUID tenantId, AiSystem system) {
    this.id = system.id();
    this.tenantId = tenantId;
    this.name = system.name();
    this.owner = system.owner();
    this.purpose = system.purpose();
    this.riskClass = system.riskClass();
    this.riskBasis = system.riskBasis();
    this.deploymentRegion = system.deploymentRegion();
    this.evidenceCoverage = system.evidenceCoverage();
    this.evalScore = system.evalScore();
    this.dataContractStatus = system.dataContractStatus();
    this.releaseDecision = system.releaseDecision();
    this.openGaps = system.openGaps();
    this.createdAt = system.createdAt();
    this.updatedAt = system.updatedAt();
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public AiSystem toDomain() {
    return new AiSystem(
        id,
        name,
        owner,
        purpose,
        riskClass,
        riskBasis,
        deploymentRegion,
        evidenceCoverage,
        evalScore,
        dataContractStatus,
        releaseDecision,
        openGaps,
        createdAt,
        updatedAt);
  }
}
