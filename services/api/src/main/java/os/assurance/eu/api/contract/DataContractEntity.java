package os.assurance.eu.api.contract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import os.assurance.eu.api.system.DataContractStatus;

@Entity
@Table(name = "data_contracts")
public class DataContractEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String owner;

  @Column(nullable = false)
  private String version;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DataContractStatus status;

  @Column(nullable = false)
  private int coverage;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected DataContractEntity() {
  }

  public DataContractEntity(UUID tenantId, DataContract contract) {
    this.id = contract.id();
    this.tenantId = tenantId;
    this.systemId = contract.systemId();
    this.name = contract.name();
    this.owner = contract.owner();
    this.version = contract.version();
    this.status = contract.status();
    this.coverage = contract.coverage();
    this.createdAt = contract.createdAt();
    this.updatedAt = contract.updatedAt();
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public UUID systemId() {
    return systemId;
  }

  public DataContractStatus status() {
    return status;
  }

  public DataContract toDomain() {
    return new DataContract(id, systemId, name, owner, version, status, coverage, createdAt, updatedAt);
  }
}
