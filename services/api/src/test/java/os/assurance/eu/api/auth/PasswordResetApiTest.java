package os.assurance.eu.api.auth;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test"
})
@AutoConfigureMockMvc
class PasswordResetApiTest {
  private static final String OLD = "correct-horse-battery";
  private static final String NEW = "new-long-password-42";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper json;
  @MockitoSpyBean EmailSender emailSender;

  private String lastToken(String to) {
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getAllValues().stream().filter(m -> to.equals(m.to()))
        .reduce((a, b) -> b).orElseThrow().textBody();
    return body.substring(body.indexOf("token=") + 6).split("\\s")[0];
  }

  private String call(String path, String body, int expected) throws Exception {
    return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
  }

  private String signedUpVerifiedEmail() throws Exception {
    String email = "reset-" + UUID.randomUUID() + "@acme.example";
    call("/auth/signup", "{\"email\":\"" + email + "\",\"password\":\"" + OLD + "\",\"organisationName\":\"Acme\"}", 201);
    call("/auth/verify-email", "{\"token\":\"" + lastToken(email) + "\"}", 200);
    return email;
  }

  @Test
  void forgotThenResetChangesPasswordAndRevokesOldSessions() throws Exception {
    String email = signedUpVerifiedEmail();
    String login = call("/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + OLD + "\"}", 200);
    String oldRefresh = json.readTree(login).get("refreshToken").asText();

    call("/auth/password/forgot", "{\"email\":\"" + email.toUpperCase() + "\"}", 202);
    String token = lastToken(email);

    call("/auth/password/reset", "{\"token\":\"" + token + "\",\"newPassword\":\"" + NEW + "\"}", 204);

    call("/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + NEW + "\"}", 200);
    call("/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + OLD + "\"}", 401);
    call("/auth/refresh", "{\"refreshToken\":\"" + oldRefresh + "\"}", 401);
    call("/auth/password/reset", "{\"token\":\"" + token + "\",\"newPassword\":\"" + NEW + "x\"}", 410);
  }

  @Test
  void forgotForUnknownEmailIsAcceptedAndSendsNothing() throws Exception {
    String unknown = "ghost-" + UUID.randomUUID() + "@nowhere.example";
    clearInvocations(emailSender);
    call("/auth/password/forgot", "{\"email\":\"" + unknown + "\"}", 202);
    verify(emailSender, never()).send(argThat(m -> unknown.equals(m.to())));
  }

  @Test
  void resetRejectsShortPasswordWithoutConsumingTheToken() throws Exception {
    String email = signedUpVerifiedEmail();
    call("/auth/password/forgot", "{\"email\":\"" + email + "\"}", 202);
    String token = lastToken(email);
    call("/auth/password/reset", "{\"token\":\"" + token + "\",\"newPassword\":\"short\"}", 400);
    call("/auth/password/reset", "{\"token\":\"" + token + "\",\"newPassword\":\"" + NEW + "\"}", 204);
  }

  @Test
  void unverifiedUserCanVerifyThroughReset() throws Exception {
    String email = "unverified-" + UUID.randomUUID() + "@acme.example";
    call("/auth/signup", "{\"email\":\"" + email + "\",\"password\":\"" + OLD + "\",\"organisationName\":\"Acme\"}", 201);
    call("/auth/password/forgot", "{\"email\":\"" + email + "\"}", 202);
    call("/auth/password/reset", "{\"token\":\"" + lastToken(email) + "\",\"newPassword\":\"" + NEW + "\"}", 204);
    call("/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + NEW + "\"}", 200);
  }
}
