package os.assurance.eu.api.auth;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import os.assurance.eu.api.email.EmailSender;

/** Per-address cap on emailed links: silent (same 202 either way) and never an account lock. */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000",
    "assurance.auth.email.cooldown-seconds=0",
    "assurance.auth.email.max-per-hour=2"
})
@AutoConfigureMockMvc
class EmailSendThrottleTest {
  @Autowired MockMvc mockMvc;
  @MockitoSpyBean EmailSender emailSender;

  private void post202(String path, String body) throws Exception {
    mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void resetLinksStopAfterTheHourlyCapButStillAnswerAccepted() throws Exception {
    String email = "cap-" + UUID.randomUUID() + "@acme.example";
    post202("/auth/signup", "{\"email\":\"" + email + "\",\"organisationName\":\"Acme\"}");
    clearInvocations(emailSender);
    for (int i = 0; i < 4; i++) {
      post202("/auth/password/forgot", "{\"email\":\"" + email + "\"}");
    }
    verify(emailSender, times(2)).send(argThat(m -> email.equals(m.to())));
  }

  @Test
  void verificationLinksStopAfterTheHourlyCapAcrossSignupAndResend() throws Exception {
    String email = "vcap-" + UUID.randomUUID() + "@acme.example";
    post202("/auth/signup", "{\"email\":\"" + email + "\",\"organisationName\":\"Acme\"}");
    for (int i = 0; i < 3; i++) {
      post202("/auth/verify-email/resend", "{\"email\":\"" + email + "\"}");
    }
    post202("/auth/signup", "{\"email\":\"" + email + "\",\"organisationName\":\"Acme\"}");
    verify(emailSender, times(2)).send(argThat(m -> email.equals(m.to())));
  }
}
