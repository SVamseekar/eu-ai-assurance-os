package os.assurance.eu.api.evidence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EvidenceQueryRequest(@NotNull UUID systemId, @NotBlank @Size(max = 2048) String question) {
}
