package os.assurance.eu.api.determination;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateDeterminationRunRequest(
    @NotNull Map<String, Object> answers) {
}
