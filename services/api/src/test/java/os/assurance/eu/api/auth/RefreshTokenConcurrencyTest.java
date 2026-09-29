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
  void parallelRotationsOfTheSameTokenAllKeepTheSessionAlive() throws Exception {
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
    List<RefreshTokenService.RefreshResult.Rotated> rotated = new ArrayList<>();
    for (Future<RefreshTokenService.RefreshResult> f : results) {
      assertThat(f.get()).isInstanceOf(RefreshTokenService.RefreshResult.Rotated.class);
      rotated.add((RefreshTokenService.RefreshResult.Rotated) f.get());
    }
    pool.shutdown();
    // Every token handed out must still work: no parallel request may revoke another's session.
    for (RefreshTokenService.RefreshResult.Rotated r : rotated) {
      assertThat(service.rotate(r.newToken().rawToken()))
          .isInstanceOf(RefreshTokenService.RefreshResult.Rotated.class);
    }
  }
}
