package os.assurance.eu.api.eval;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CompleteEvalRunRequest(@NotNull Map<String, Object> metrics) {
}
