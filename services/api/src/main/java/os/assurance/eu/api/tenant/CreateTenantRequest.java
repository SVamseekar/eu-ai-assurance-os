package os.assurance.eu.api.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 64) String plan,
    @Size(max = 32) String dataRegion,
    @NotBlank @Email String adminEmail,
    @NotBlank @Size(min = 12, max = 128) String adminPassword) {
}
