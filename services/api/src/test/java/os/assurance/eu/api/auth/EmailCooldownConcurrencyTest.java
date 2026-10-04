package os.assurance.eu.api.auth;

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

/** The cooldown is check-then-send; parallel requests for one address must still send once. */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test"
})
class EmailCooldownConcurrencyTest {
  @Autowired SignupService signup;
  @Autowired PasswordResetService reset;
  @MockitoSpyBean EmailSender emailSender;

  @Test
  void parallelResetRequestsForOneAddressSendOneEmail() throws Exception {
    String email = "race-" + UUID.randomUUID() + "@acme.example";
    signup.signup(new SignupRequest(email, "Acme"));
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> done = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      done.add(pool.submit(() -> {
        start.await();
        reset.forgot(email);
        return null;
      }));
    }
    start.countDown();
    for (Future<?> f : done) {
      f.get();
    }
    pool.shutdown();
    // one verification email from signup + exactly one reset email
    verify(emailSender, times(2)).send(argThat(m -> email.equals(m.to())));
  }
}
