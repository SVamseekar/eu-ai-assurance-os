package os.assurance.eu.api.control;

import jakarta.validation.constraints.NotNull;

public record UpdateSystemControlRequest(
    @NotNull ControlStatus status,
    String notes) {
}
