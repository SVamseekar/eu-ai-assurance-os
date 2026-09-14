package os.assurance.eu.api.conformity;

import java.util.Map;

public record UpdateConformityDossierRequest(
    Map<String, Object> annexIv,
    Map<String, Object> fria,
    Map<String, Object> declarationOfConformity,
    Map<String, Object> art49Registration) {
}
