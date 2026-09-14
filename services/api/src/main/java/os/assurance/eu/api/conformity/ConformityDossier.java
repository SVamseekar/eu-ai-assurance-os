package os.assurance.eu.api.conformity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ConformityDossier(
    UUID id,
    UUID systemId,
    Map<String, Object> annexIv,
    Map<String, Object> fria,
    Map<String, Object> declarationOfConformity,
    Map<String, Object> art49Registration,
    Instant updatedAt,
    UUID updatedBy,
    String disclaimer) {

  public static ConformityDossier from(ConformityDossierEntity entity) {
    return new ConformityDossier(
        entity.id(),
        entity.systemId(),
        entity.annexIv(),
        entity.fria(),
        entity.declarationOfConformity(),
        entity.art49Registration(),
        entity.updatedAt(),
        entity.updatedBy(),
        ConformityTemplates.DISCLAIMER);
  }
}
