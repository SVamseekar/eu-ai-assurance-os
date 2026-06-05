package os.assurance.eu.api.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DriftEventRequest(
    @NotNull DriftSeverity severity,
    String field,
    @NotBlank String description) {
}
