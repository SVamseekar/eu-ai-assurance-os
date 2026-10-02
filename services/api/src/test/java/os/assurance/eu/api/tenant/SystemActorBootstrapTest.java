package os.assurance.eu.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import os.assurance.eu.api.audit.AuditEvent;
import os.assurance.eu.api.audit.AuditService;

@SpringBootTest(properties = {
    "assurance.bootstrap.seed-demo-users=false",
    "spring.datasource.url=jdbc:h2:mem:system_actor;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
class SystemActorBootstrapTest {
  @Autowired
  private UserJpaRepository users;
  @Autowired
  private TenantContext tenantContext;
  @Autowired
  private AuditService auditService;

  @Test
  void bootstrapCreatesSystemActorThatCannotLogIn() {
    UserEntity system = users.findById(TenantContext.SYSTEM_USER_ID).orElseThrow();

    assertThat(system.tenantId()).isEqualTo(TenantContext.DEFAULT_TENANT_ID);
    assertThat(system.passwordHash()).isNull();
  }

  @Test
  void backgroundJobsCanAuditWithoutDemoUsers() {
    tenantContext.setOverrides(TenantContext.DEFAULT_TENANT_ID, TenantContext.SYSTEM_USER_ID);
    try {
      AuditEvent event = auditService.append(null, "system.test", "test", "1", Map.of());
      assertThat(event.actorId()).isEqualTo(TenantContext.SYSTEM_USER_ID);
    } finally {
      tenantContext.clearOverrides();
    }
  }
}
