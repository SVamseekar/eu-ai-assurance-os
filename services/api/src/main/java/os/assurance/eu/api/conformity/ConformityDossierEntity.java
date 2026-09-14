package os.assurance.eu.api.conformity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.persistence.JsonMapConverter;

@Entity
@Table(name = "conformity_dossiers")
public class ConformityDossierEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Convert(converter = JsonMapConverter.class)
  @Column(name = "annex_iv", nullable = false)
  private Map<String, Object> annexIv;

  @Convert(converter = JsonMapConverter.class)
  @Column(nullable = false)
  private Map<String, Object> fria;

  @Convert(converter = JsonMapConverter.class)
  @Column(name = "declaration_of_conformity", nullable = false)
  private Map<String, Object> declarationOfConformity;

  @Convert(converter = JsonMapConverter.class)
  @Column(name = "art49_registration", nullable = false)
  private Map<String, Object> art49Registration;

  @Column(nullable = false)
  private Instant updatedAt;

  private UUID updatedBy;

  protected ConformityDossierEntity() {
  }

  public ConformityDossierEntity(
      UUID id,
      UUID tenantId,
      UUID systemId,
      Map<String, Object> annexIv,
      Map<String, Object> fria,
      Map<String, Object> declarationOfConformity,
      Map<String, Object> art49Registration,
      Instant updatedAt,
      UUID updatedBy) {
    this.id = id;
    this.tenantId = tenantId;
    this.systemId = systemId;
    this.annexIv = annexIv;
    this.fria = fria;
    this.declarationOfConformity = declarationOfConformity;
    this.art49Registration = art49Registration;
    this.updatedAt = updatedAt;
    this.updatedBy = updatedBy;
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

  public Map<String, Object> annexIv() {
    return annexIv;
  }

  public Map<String, Object> fria() {
    return fria;
  }

  public Map<String, Object> declarationOfConformity() {
    return declarationOfConformity;
  }

  public Map<String, Object> art49Registration() {
    return art49Registration;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public UUID updatedBy() {
    return updatedBy;
  }

  public void replace(
      Map<String, Object> annexIv,
      Map<String, Object> fria,
      Map<String, Object> declarationOfConformity,
      Map<String, Object> art49Registration,
      Instant updatedAt,
      UUID updatedBy) {
    this.annexIv = annexIv;
    this.fria = fria;
    this.declarationOfConformity = declarationOfConformity;
    this.art49Registration = art49Registration;
    this.updatedAt = updatedAt;
    this.updatedBy = updatedBy;
  }
}
