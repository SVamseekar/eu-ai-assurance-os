package os.assurance.eu.api.contract;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import os.assurance.eu.api.system.DataContractStatus;

public record CreateDataContractRequest(
    @NotNull UUID systemId,
    @NotBlank String name,
    @NotBlank String owner,
    @NotBlank String version,
    DataContractStatus status,
    @Min(0) @Max(100) Integer coverage) {
}
