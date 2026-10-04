package os.assurance.eu.api.auth;

import static org.mockito.ArgumentMatchers.argThat;
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

/** Default 60 s cooldown per address and purpose. */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000"
})
@AutoConfigureMockMvc
class EmailCooldownTest {
  @Autowired MockMvc mockMvc;
  @MockitoSpyBean EmailSender emailSender;

  private void post2xx(String path, String body) throws Exception {
    mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void rapidRepeatsSendOnlyOneEmailPerPurposeAndAlwaysAnswerTheSame() throws Exception {
    String email = "cool-" + UUID.randomUUID() + "@acme.example";
    post2xx("/auth/signup", "{\"email\":\"" + email + "\",\"organisationName\":\"Acme\"}");
    post2xx("/auth/verify-email/resend", "{\"email\":\"" + email + "\"}");
    post2xx("/auth/signup", "{\"email\":\"" + email + "\",\"organisationName\":\"Acme\"}");
    verify(emailSender, times(1)).send(argThat(m -> email.equals(m.to())));

    post2xx("/auth/password/forgot", "{\"email\":\"" + email + "\"}");
    post2xx("/auth/password/forgot", "{\"email\":\"" + email + "\"}");
    verify(emailSender, times(2)).send(argThat(m -> email.equals(m.to())));
  }
}
