package os.assurance.eu.api;

import static org.assertj.core.api.Assertions.assertThat;

import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "assurance.eval.callback.secret=ci-eval-callback-secret-for-postgres-smoke-only",
    "assurance.audit.chain-secret=ci-audit-chain-secret-for-postgres-smoke-only",
    "assurance.oauth.state-secret=ci-oauth-state-secret-for-postgres-smoke-only",
    "assurance.auth.key-encryption-secret=ci-jwt-key-encryption-secret-for-postgres-smoke"
})
@ActiveProfiles("postgres")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_SMOKE", matches = "true")
@EnabledIfEnvironmentVariable(named = "DATABASE_URL", matches = ".+")
class PostgresProfileSmokeTest {
  @Autowired
  private AiSystemRepository systems;
  @Autowired
  private TenantJpaRepository tenants;
  @Autowired
  private TenantContext tenantContext;

  @Test
  void startsWithPostgresProfileAndRunsFlywayBootstrap() {
    assertThat(tenantContext.withTenant(TenantContext.DEFAULT_TENANT_ID, systems::findAll)).isNotNull();
    assertThat(tenants.findById(TenantContext.DEFAULT_TENANT_ID)).isPresent();
  }
}
