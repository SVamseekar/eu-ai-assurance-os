package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.tenant.UserJpaRepository;

/** Check-then-create on a brand-new address: parallel signups must yield one account and never an error. */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test"
})
class ConcurrentSignupTest {
  @Autowired SignupService signup;
  @Autowired UserJpaRepository users;
  @MockitoSpyBean EmailSender emailSender;

  @Test
  void parallelSignupsForANewAddressCreateOneAccountAndNeverFail() throws Exception {
    String email = "twin-" + UUID.randomUUID() + "@acme.example";
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> done = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      done.add(pool.submit(() -> {
        start.await();
        signup.signup(new SignupRequest(email, "Acme"));
        return null;
      }));
    }
    start.countDown();
    for (Future<?> f : done) {
      f.get();
    }
    pool.shutdown();

    assertThat(users.findAll().stream().filter(u -> email.equalsIgnoreCase(u.email()))).hasSize(1);
    verify(emailSender, times(1)).send(argThat(m -> email.equals(m.to())));
  }
}
