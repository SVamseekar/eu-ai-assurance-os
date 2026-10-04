package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import os.assurance.eu.api.email.EmailMessage;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserJpaRepository;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test"
})
@AutoConfigureMockMvc
class SignupApiTest {
  @Autowired MockMvc mockMvc;
  @Autowired UserJpaRepository users;
  @Autowired TenantJpaRepository tenants;
  @MockitoSpyBean EmailSender emailSender;

  private String signup(String email) throws Exception {
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"correct-horse-battery\",\"organisationName\":\"Acme AI\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("verification_sent"));
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    org.mockito.Mockito.verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getValue().textBody();
    return body.substring(body.indexOf("token=") + 6).split("\\s")[0];
  }

  @Test
  void signupCreatesTrialTenantAdminAndRequiresVerificationBeforeLogin() throws Exception {
    String email = "founder-" + UUID.randomUUID() + "@acme.example";
    String token = signup(email);

    var user = users.findByEmailIgnoreCase(email).orElseThrow();
    assertThat(user.role().name()).isEqualTo("ADMIN");
    assertThat(user.emailVerifiedAt()).isNull();
    assertThat(tenants.findById(user.tenantId()).orElseThrow().plan()).isEqualTo("trial");

    mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"correct-horse-battery\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("email_not_verified"));

    mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty());

    mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\"}"))
        .andExpect(status().isGone());

    mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"correct-horse-battery\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void duplicateEmailInAnyCaseIsRejected() throws Exception {
    String email = "dup-" + UUID.randomUUID() + "@acme.example";
    signup(email);
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email.toUpperCase() + "\",\"password\":\"correct-horse-battery\",\"organisationName\":\"Other\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void weakPasswordAndMissingOrgAreRejected() throws Exception {
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"weak@acme.example\",\"password\":\"short\",\"organisationName\":\"Acme\"}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"noorg@acme.example\",\"password\":\"correct-horse-battery\",\"organisationName\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void resendNeverRevealsWhetherAnEmailExists() throws Exception {
    mockMvc.perform(post("/auth/verify-email/resend").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"nobody-" + UUID.randomUUID() + "@nowhere.example\"}"))
        .andExpect(status().isAccepted());
  }
}
