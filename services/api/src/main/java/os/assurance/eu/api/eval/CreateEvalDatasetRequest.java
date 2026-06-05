package os.assurance.eu.api.eval;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEvalDatasetRequest(
    @NotBlank String name,
    @NotBlank String version,
    @NotNull @Min(1) Integer sampleCount,
    boolean golden) {
}
