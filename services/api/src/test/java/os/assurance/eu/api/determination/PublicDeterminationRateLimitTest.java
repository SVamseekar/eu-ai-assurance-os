package os.assurance.eu.api.determination;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** The public preview shares the per-IP auth rate limit, so bots cannot hammer it. */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.security.auth-rate.per-ip-per-15m=2"
})
@AutoConfigureMockMvc
class PublicDeterminationRateLimitTest {
  @Autowired MockMvc mockMvc;

  @Test
  void previewIsRateLimitedPerIp() throws Exception {
    String body = "{\"answers\":{\"sector\":\"other\",\"interacts_with_natural_persons\":true}}";
    for (int i = 0; i < 2; i++) {
      mockMvc.perform(post("/api/public/v1/determination/preview").with(r -> {
            r.setRemoteAddr("203.0.113.9");
            return r;
          }).contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isOk());
    }
    mockMvc.perform(post("/api/public/v1/determination/preview").with(r -> {
          r.setRemoteAddr("203.0.113.9");
          return r;
        }).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isTooManyRequests());
  }
}
