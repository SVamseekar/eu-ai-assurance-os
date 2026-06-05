package os.assurance.eu.api.contract;

import jakarta.validation.constraints.NotNull;

public record UpdateDriftEventRequest(@NotNull DriftStatus status) {
}
