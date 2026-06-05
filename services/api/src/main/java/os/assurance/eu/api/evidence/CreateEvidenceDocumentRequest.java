package os.assurance.eu.api.evidence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record CreateEvidenceDocumentRequest(
    @NotNull UUID systemId,
    @NotBlank String type,
    @NotBlank String title,
    @NotBlank String sourceUri,
    String content,
    String checksum,
    Map<String, Object> metadata) {
}
