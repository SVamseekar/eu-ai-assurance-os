package os.assurance.eu.api.regmonitor;

import os.assurance.eu.api.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Loads curated bootstrap fixtures on startup when network is blocked / sources empty.
 *
 * <p>Must run <em>after</em> {@code BootstrapData} (also a {@link CommandLineRunner}) so the
 * default tenant exists for audit appends. ApplicationRunner beans run before CommandLineRunner
 * in Spring Boot, so this intentionally uses CommandLineRunner + high order.
 */
@Component
@Order(1000)
@ConditionalOnProperty(
    name = "assurance.reg-monitor.bootstrap-fixtures",
    havingValue = "true",
    matchIfMissing = true)
public class RegMonitorBootstrapRunner implements CommandLineRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegMonitorBootstrapRunner.class);

  private final RegMonitorIngestionService ingestionService;
  private final RegItemJpaRepository items;
  private final TenantContext tenantContext;

  public RegMonitorBootstrapRunner(
      RegMonitorIngestionService ingestionService,
      RegItemJpaRepository items,
      TenantContext tenantContext) {
    this.ingestionService = ingestionService;
    this.items = items;
    this.tenantContext = tenantContext;
  }

  @Override
  public void run(String... args) {
    if (items.count() > 0) {
      return;
    }
    tenantContext.setOverrides(TenantContext.DEFAULT_TENANT_ID, TenantContext.DEFAULT_USER_ID);
    try {
      int n = ingestionService.ensureBootstrapFixtures();
      LOGGER.info("Reg-monitor bootstrap fixtures loaded: {} item(s)", n);
    } catch (RuntimeException ex) {
      LOGGER.warn("Reg-monitor bootstrap failed: {}", ex.getMessage());
    } finally {
      tenantContext.clearOverrides();
    }
  }
}
