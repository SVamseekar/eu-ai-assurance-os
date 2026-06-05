package os.assurance.eu.api.evidence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

public record CreateEvidenceDocumentRequest(
    @NotNull UUID systemId,
    @NotBlank @Size(max = 128) String type,
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 2048) String sourceUri,
    @Size(max = 200000) String content,
    @Size(max = 128) String checksum,
    @Size(max = 25) Map<String, Object> metadata) {
}
