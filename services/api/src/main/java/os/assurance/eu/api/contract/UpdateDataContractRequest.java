package os.assurance.eu.api.contract;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import os.assurance.eu.api.system.DataContractStatus;

public record UpdateDataContractRequest(
    String name,
    String owner,
    String version,
    DataContractStatus status,
    @Min(0) @Max(100) Integer coverage) {
}
