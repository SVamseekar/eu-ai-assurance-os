package os.assurance.eu.api.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** A spoofed or rotating client address must not lift the cap on the shared demo workspace. */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.demo.enabled=true",
    "assurance.demo.query-limit-per-15m=100",
    "assurance.demo.query-limit-global-per-15m=5"
})
@AutoConfigureMockMvc
class DemoGlobalCapApiTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper json;

  @Test
  void aWorkspaceWideCapHoldsEvenIfClientAddressesAreRotated() throws Exception {
    String bearer = "Bearer " + json.readTree(mockMvc.perform(post("/auth/demo")).andReturn().getResponse()
        .getContentAsString()).get("accessToken").asText();
    String systems = mockMvc.perform(get("/api/v1/systems").header("Authorization", bearer))
        .andReturn().getResponse().getContentAsString();
    String id = null;
    for (JsonNode s : json.readTree(systems)) {
      if (s.get("name").asText().startsWith("Claims Triage")) id = s.get("id").asText();
    }
    String question = "{\"systemId\":\"" + id + "\",\"question\":\"Who can override routing?\"}";
    int ok = 0;
    int limited = 0;
    for (int i = 0; i < 12; i++) {
      final String ip = "198.51.100." + (100 + i);
      int code = mockMvc.perform(post("/api/v1/evidence/query").header("Authorization", bearer)
              .with(r -> { r.setRemoteAddr(ip); return r; })
              .contentType(MediaType.APPLICATION_JSON).content(question))
          .andReturn().getResponse().getStatus();
      if (code == 200) ok++;
      if (code == 429) limited++;
    }
    assertThat(ok).isEqualTo(5);
    assertThat(limited).isEqualTo(7);
  }
}
