package os.assurance.eu.api.eval;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateEvalRunRequest(
    @NotNull UUID systemId,
    @NotBlank String dataset,
    @NotBlank String modelVersion,
    @NotBlank String promptVersion,
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double threshold) {
}
