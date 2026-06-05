package os.assurance.eu.api.evidence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EvidenceQueryRequest(@NotNull UUID systemId, @NotBlank String question) {
}
