package os.assurance.eu.api.tenant;

public record CreateTenantResponse(TenantView tenant, UserView admin) {
}
