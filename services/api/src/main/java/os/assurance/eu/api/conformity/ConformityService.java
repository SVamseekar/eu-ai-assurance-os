package os.assurance.eu.api.conformity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConformityService {
  private final ConformityDossierJpaRepository dossiers;
  private final AiSystemRepository systems;
  private final TenantContext tenantContext;
  private final AuditService auditService;

  public ConformityService(
      ConformityDossierJpaRepository dossiers,
      AiSystemRepository systems,
      TenantContext tenantContext,
      AuditService auditService) {
    this.dossiers = dossiers;
    this.systems = systems;
    this.tenantContext = tenantContext;
    this.auditService = auditService;
  }

  @Transactional
  public ConformityDossier getOrCreate(UUID systemId) {
    requireSystem(systemId);
    return ConformityDossier.from(loadOrCreate(systemId));
  }

  @Transactional
  public ConformityDossier update(UUID systemId, UpdateConformityDossierRequest request) {
    requireSystem(systemId);
    ConformityDossierEntity entity = loadOrCreate(systemId);
    Instant now = Instant.now();
    entity.replace(
        request.annexIv() == null ? entity.annexIv() : request.annexIv(),
        request.fria() == null ? entity.fria() : request.fria(),
        request.declarationOfConformity() == null
            ? entity.declarationOfConformity()
            : request.declarationOfConformity(),
        request.art49Registration() == null ? entity.art49Registration() : request.art49Registration(),
        now,
        tenantContext.actorId());
    dossiers.save(entity);
    auditService.append(
        systemId,
        "conformity.dossier.updated",
        "conformity_dossier",
        entity.id().toString(),
        Map.of("disclaimer", ConformityTemplates.DISCLAIMER));
    return ConformityDossier.from(entity);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> snapshotForPack(UUID systemId) {
    Map<String, Object> snap = new LinkedHashMap<>();
    snap.put("disclaimer", ConformityTemplates.DISCLAIMER);
    dossiers.findByTenantIdAndSystemId(tenantContext.tenantId(), systemId)
        .ifPresentOrElse(
            entity -> {
              snap.put("annexIv", entity.annexIv());
              snap.put("fria", entity.fria());
              snap.put("declarationOfConformity", entity.declarationOfConformity());
              snap.put("art49Registration", entity.art49Registration());
              snap.put("updatedAt", entity.updatedAt().toString());
            },
            () -> {
              snap.put("annexIv", ConformityTemplates.emptyAnnexIv());
              snap.put("fria", ConformityTemplates.emptyFria());
              snap.put("declarationOfConformity", ConformityTemplates.emptyDeclaration());
              snap.put("art49Registration", ConformityTemplates.emptyArt49());
              snap.put("status", "NONE");
            });
    return snap;
  }

  public List<String> reviewBlockers(UUID systemId, RiskClass riskClass) {
    if (riskClass != RiskClass.HIGH) {
      return List.of();
    }
    ConformityDossierEntity entity = dossiers
        .findByTenantIdAndSystemId(tenantContext.tenantId(), systemId)
        .orElse(null);
    Map<String, Object> annex = entity == null ? ConformityTemplates.emptyAnnexIv() : entity.annexIv();
    List<String> blockers = new ArrayList<>();
    Object sections = annex.get("sections");
    if (sections instanceof List<?> list) {
      for (Object item : list) {
        if (item instanceof Map<?, ?> row) {
          Object id = row.get("id");
          Object status = row.get("status");
          if (status == null || "MISSING".equalsIgnoreCase(String.valueOf(status))) {
            blockers.add("ANNEX_IV_INCOMPLETE:" + (id == null ? "unknown" : id));
          }
        }
      }
    }
    Map<String, Object> fria = entity == null ? ConformityTemplates.emptyFria() : entity.fria();
    if (!"RECORDED".equalsIgnoreCase(String.valueOf(fria.getOrDefault("status", "")))
        && !"WAIVED".equalsIgnoreCase(String.valueOf(fria.getOrDefault("status", "")))) {
      blockers.add("FRIA_NOT_RECORDED");
    }
    return blockers;
  }

  private ConformityDossierEntity loadOrCreate(UUID systemId) {
    return dossiers.findByTenantIdAndSystemId(tenantContext.tenantId(), systemId)
        .orElseGet(() -> dossiers.save(new ConformityDossierEntity(
            UUID.randomUUID(),
            tenantContext.tenantId(),
            systemId,
            ConformityTemplates.emptyAnnexIv(),
            ConformityTemplates.emptyFria(),
            ConformityTemplates.emptyDeclaration(),
            ConformityTemplates.emptyArt49(),
            Instant.now(),
            tenantContext.actorId())));
  }

  private AiSystem requireSystem(UUID systemId) {
    return systems.findById(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
  }
}
