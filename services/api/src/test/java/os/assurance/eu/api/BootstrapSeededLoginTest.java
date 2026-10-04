package os.assurance.eu.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Local development and the e2e suite sign in as the seeded users, so they must not be stuck unverified. */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.security.auth-rate.per-ip-per-15m=1000"
})
@AutoConfigureMockMvc
class BootstrapSeededLoginTest {
  @Autowired MockMvc mockMvc;

  @Test
  void seededDemoUsersCanSignInWithoutConfirmingAnEmail() throws Exception {
    for (String email : new String[] {"compliance@example.com", "engineering@example.com", "auditor@example.com",
        "legal@example.com", "admin@example.com"}) {
      mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
              .content("{\"email\":\"" + email + "\",\"password\":\"dev-local-password-only\"}"))
          .andExpect(status().isOk());
    }
  }
}
