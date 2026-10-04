package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000",
    "assurance.auth.email.cooldown-seconds=0"
})
@AutoConfigureMockMvc
class SignupApiTest {
  private static final String PASSWORD = "correct-horse-battery";

  @Autowired MockMvc mockMvc;
  @Autowired UserJpaRepository users;
  @Autowired TenantJpaRepository tenants;
  @MockitoSpyBean EmailSender emailSender;

  private void signup(String email, String org) throws Exception {
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"organisationName\":\"" + org + "\"}"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("verification_sent"));
  }

  private EmailMessage lastEmailTo(String to) {
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    return captor.getAllValues().stream().filter(m -> to.equalsIgnoreCase(m.to()))
        .reduce((a, b) -> b).orElseThrow();
  }

  private String signupToken(String email) throws Exception {
    signup(email, "Acme AI");
    String body = lastEmailTo(email).textBody();
    return body.substring(body.indexOf("token=") + 6).split("\\s")[0];
  }

  private void verifyEmail(String token, String password, int expected) throws Exception {
    mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().is(expected));
  }

  private void login(String email, String password, int expected) throws Exception {
    mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().is(expected));
  }

  @Test
  void signupCreatesPasswordlessTrialAdminAndPasswordIsChosenOnVerify() throws Exception {
    String email = "founder-" + UUID.randomUUID() + "@acme.example";
    String token = signupToken(email);

    var user = users.findByEmailIgnoreCase(email).orElseThrow();
    assertThat(user.role().name()).isEqualTo("ADMIN");
    assertThat(user.emailVerifiedAt()).isNull();
    assertThat(user.passwordHash()).isNull();
    assertThat(tenants.findById(user.tenantId()).orElseThrow().plan()).isEqualTo("trial");
    login(email, PASSWORD, 401);

    mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty());
    verifyEmail(token, PASSWORD, 410);
    login(email, PASSWORD, 200);
    assertThat(users.findByEmailIgnoreCase(email).orElseThrow().emailVerifiedAt()).isNotNull();
  }

  @Test
  void verifyRejectsShortPasswordWithoutConsumingTheToken() throws Exception {
    String email = "short-" + UUID.randomUUID() + "@acme.example";
    String token = signupToken(email);
    verifyEmail(token, "short", 400);
    assertThat(users.findByEmailIgnoreCase(email).orElseThrow().emailVerifiedAt()).isNull();
    verifyEmail(token, PASSWORD, 200);
  }

  @Test
  void signupForVerifiedEmailChangesNothingAndSendsAccountExistsNotice() throws Exception {
    String email = "owner-" + UUID.randomUUID() + "@acme.example";
    verifyEmail(signupToken(email), PASSWORD, 200);
    var before = users.findByEmailIgnoreCase(email).orElseThrow();
    long tenantsBefore = tenants.count();

    clearInvocations(emailSender);
    signup(email.toUpperCase(), "Attacker Org");

    assertThat(tenants.count()).isEqualTo(tenantsBefore);
    var after = users.findByEmailIgnoreCase(email).orElseThrow();
    assertThat(after.passwordHash()).isEqualTo(before.passwordHash());
    assertThat(tenants.findById(after.tenantId()).orElseThrow().name()).isEqualTo("Acme AI");
    EmailMessage notice = lastEmailTo(email);
    assertThat(notice.textBody()).contains("already has an account").contains("/reset-password?token=");
    login(email, PASSWORD, 200);
  }

  @Test
  void signupForUnverifiedEmailReusesTenantAndTheLatestOrgNameWins() throws Exception {
    String email = "pending-" + UUID.randomUUID() + "@acme.example";
    String attackerLink = signupToken(email);
    long tenantsAfterFirst = tenants.count();

    signup(email.toUpperCase(), "Real Org");
    String bodyOfSecond = lastEmailTo(email).textBody();
    String victimLink = bodyOfSecond.substring(bodyOfSecond.indexOf("token=") + 6).split("\\s")[0];

    assertThat(tenants.count()).isEqualTo(tenantsAfterFirst);
    assertThat(victimLink).isNotEqualTo(attackerLink);
    var user = users.findByEmailIgnoreCase(email).orElseThrow();
    assertThat(tenants.findById(user.tenantId()).orElseThrow().name()).isEqualTo("Real Org");
    verifyEmail(victimLink, PASSWORD, 200);
    verifyEmail(attackerLink, "another-long-password", 410);
  }

  @Test
  void weakInputAndMissingOrgAreRejected() throws Exception {
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"not-an-email\",\"organisationName\":\"Acme\"}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"noorg@acme.example\",\"organisationName\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void resendNeverRevealsWhetherAnEmailExists() throws Exception {
    String unknown = "nobody-" + UUID.randomUUID() + "@nowhere.example";
    clearInvocations(emailSender);
    mockMvc.perform(post("/auth/verify-email/resend").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + unknown + "\"}"))
        .andExpect(status().isAccepted());
    verify(emailSender, never()).send(argThat(m -> unknown.equals(m.to())));
  }

  @Test
  void resendIssuesAFreshLinkAndVerifyingRetiresEarlierOnes() throws Exception {
    String email = "resend-" + UUID.randomUUID() + "@acme.example";
    String first = signupToken(email);
    mockMvc.perform(post("/auth/verify-email/resend").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\"}"))
        .andExpect(status().isAccepted());
    String body = lastEmailTo(email).textBody();
    String second = body.substring(body.indexOf("token=") + 6).split("\\s")[0];
    assertThat(second).isNotEqualTo(first);
    verifyEmail(second, PASSWORD, 200);
    verifyEmail(first, "another-long-password", 410);
  }
}
