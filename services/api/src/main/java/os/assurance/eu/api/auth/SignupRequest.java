package os.assurance.eu.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** No password here: it is chosen on the emailed link, so nobody can pre-set one for someone else's address. */
public record SignupRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 120) String organisationName) {
}
