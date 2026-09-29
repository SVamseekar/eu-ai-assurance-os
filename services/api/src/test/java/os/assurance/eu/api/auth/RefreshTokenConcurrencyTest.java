package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
class RefreshTokenConcurrencyTest {
  @Autowired RefreshTokenService service;

  @Test
  void onlyOneParallelRotationOfTheSameTokenSucceeds() throws Exception {
    var issued = service.issue(UUID.fromString("00000000-0000-0000-0000-000000000101"),
        UUID.fromString("00000000-0000-0000-0000-000000000001"));
    ExecutorService pool = Executors.newFixedThreadPool(4);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<RefreshTokenService.RefreshResult>> results = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Callable<RefreshTokenService.RefreshResult> call = () -> {
        start.await();
        return service.rotate(issued.rawToken());
      };
      results.add(pool.submit(call));
    }
    start.countDown();
    long rotated = 0;
    for (Future<RefreshTokenService.RefreshResult> f : results) {
      if (f.get() instanceof RefreshTokenService.RefreshResult.Rotated) {
        rotated++;
      }
    }
    pool.shutdown();
    assertThat(rotated).isEqualTo(1);
  }
}
