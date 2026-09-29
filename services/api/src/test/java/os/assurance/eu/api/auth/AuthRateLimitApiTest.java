package os.assurance.eu.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.security.auth-rate.per-ip-per-15m=5",
    "assurance.security.auth-rate.per-email-per-15m=3",
    "assurance.security.trust-client-ip-header=true"
})
@AutoConfigureMockMvc
class AuthRateLimitApiTest {
  @Autowired MockMvc mockMvc;

  private org.springframework.test.web.servlet.ResultActions login(String ip, String email) throws Exception {
    return mockMvc.perform(post("/auth/login")
        .header("X-Client-IP", ip)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}"));
  }

  @Test
  void perEmailLimitReturns429AfterThreeFailures() throws Exception {
    for (int i = 0; i < 3; i++) {
      login("203.0.113." + i, "target@ratelimit.example").andExpect(status().isUnauthorized());
    }
    login("203.0.113.99", "target@ratelimit.example")
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string("Retry-After", "900"));
  }

  @Test
  void perIpLimitReturns429AfterFiveRequests() throws Exception {
    for (int i = 0; i < 5; i++) {
      login("198.51.100.7", "user" + i + "@ip.example").andExpect(status().isUnauthorized());
    }
    login("198.51.100.7", "fresh@ip.example").andExpect(status().isTooManyRequests());
  }
}
