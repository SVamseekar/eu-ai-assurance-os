package os.assurance.eu.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserJpaRepository;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.bootstrap.seed-demo-users=false",
    "spring.datasource.url=jdbc:h2:mem:seeding_disabled;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
class BootstrapSeedingDisabledTest {
  @Autowired UserJpaRepository users;
  @Autowired TenantJpaRepository tenants;

  @Test
  void seedingDisabledCreatesOperatorTenantButNoKnownPasswordUsers() {
    assertThat(tenants.findById(TenantContext.DEFAULT_TENANT_ID)).isPresent();
    assertThat(users.findByEmail("admin@example.com")).isEmpty();
    assertThat(users.findByEmail("compliance@example.com")).isEmpty();
    assertThat(users.count()).isZero();
  }
}
