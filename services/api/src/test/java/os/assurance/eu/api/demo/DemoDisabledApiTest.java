package os.assurance.eu.api.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
@AutoConfigureMockMvc
class DemoDisabledApiTest {
  @Autowired MockMvc mockMvc;

  @Test
  void demoSignInIsNotFoundUnlessEnabled() throws Exception {
    mockMvc.perform(post("/auth/demo")).andExpect(status().isNotFound());
  }
}
