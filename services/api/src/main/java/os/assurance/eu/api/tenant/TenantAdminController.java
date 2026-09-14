package os.assurance.eu.api.tenant;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class TenantAdminController {
  private final TenantAdminService tenantAdminService;

  public TenantAdminController(TenantAdminService tenantAdminService) {
    this.tenantAdminService = tenantAdminService;
  }

  @PostMapping("/tenants")
  @ResponseStatus(HttpStatus.CREATED)
  public CreateTenantResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
    return tenantAdminService.createTenant(request);
  }

  @GetMapping("/users")
  public List<UserView> listUsers() {
    return tenantAdminService.listUsers();
  }

  @PostMapping("/users/invites")
  @ResponseStatus(HttpStatus.CREATED)
  public InviteCreatedResponse inviteUser(@Valid @RequestBody InviteUserRequest request) {
    return tenantAdminService.inviteUser(request);
  }

  @GetMapping("/users/invites")
  public List<InviteCreatedResponse> listInvites() {
    return tenantAdminService.listInvites();
  }
}
