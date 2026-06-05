package os.assurance.eu.api.contract;

import java.time.Instant;
import java.util.UUID;
import os.assurance.eu.api.system.DataContractStatus;

public record DataContract(
    UUID id,
    UUID systemId,
    String name,
    String owner,
    String version,
    DataContractStatus status,
    int coverage,
    Instant createdAt,
    Instant updatedAt) {
}
