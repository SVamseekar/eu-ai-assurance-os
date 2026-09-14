package os.assurance.eu.api.tenant;

import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assurance.platform")
public class PlatformProperties {
  private UUID operatorTenantId = TenantContext.DEFAULT_TENANT_ID;
  private int inviteTtlHours = 72;

  public UUID getOperatorTenantId() {
    return operatorTenantId == null ? TenantContext.DEFAULT_TENANT_ID : operatorTenantId;
  }

  public void setOperatorTenantId(UUID operatorTenantId) {
    this.operatorTenantId = operatorTenantId;
  }

  public int getInviteTtlHours() {
    return inviteTtlHours <= 0 ? 72 : inviteTtlHours;
  }

  public void setInviteTtlHours(int inviteTtlHours) {
    this.inviteTtlHours = inviteTtlHours;
  }
}
