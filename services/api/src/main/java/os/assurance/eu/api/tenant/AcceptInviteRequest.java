package os.assurance.eu.api.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 12, max = 128) String password) {
}
