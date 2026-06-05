package os.assurance.eu.api.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RiskClassificationRequest(
    @NotNull RiskClass riskClass,
    @NotBlank String basis,
    List<String> affectedUsers,
    boolean humanOversightRequired) {
}
