package os.assurance.eu.api.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import os.assurance.eu.api.tenant.TenantContext;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
class AuditChainConcurrencyTest {
  @Autowired AuditService auditService;
  @Autowired TenantContext tenantContext;

  @Test
  void parallelAppendsKeepALinearVerifiableChain() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(8);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < 40; i++) {
      int n = i;
      futures.add(pool.submit(() -> {
        start.await();
        tenantContext.setOverrides(TenantContext.DEFAULT_TENANT_ID, TenantContext.DEFAULT_USER_ID);
        try {
          auditService.append(null, "test.concurrent", "test", String.valueOf(n), Map.of("n", n));
        } finally {
          tenantContext.clearOverrides();
        }
        return null;
      }));
    }
    start.countDown();
    for (Future<?> f : futures) {
      f.get();
    }
    pool.shutdown();
    tenantContext.setOverrides(TenantContext.DEFAULT_TENANT_ID, TenantContext.DEFAULT_USER_ID);
    try {
      assertThat(auditService.verifyChain().valid()).isTrue();
    } finally {
      tenantContext.clearOverrides();
    }
  }
}
