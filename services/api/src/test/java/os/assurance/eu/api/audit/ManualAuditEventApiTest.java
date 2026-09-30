package os.assurance.eu.api.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
@AutoConfigureMockMvc
class ManualAuditEventApiTest {
  @Autowired MockMvc mockMvc;

  @Test
  void manualEventsCannotImpersonateSystemEvents() throws Exception {
    mockMvc.perform(post("/api/v1/audit-events")
            .header("X-Api-Key", "00000000-0000-0000-0000-000000000a01")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"eventType\":\"mapping_proposal.accepted\",\"resourceType\":\"note\",\"resourceId\":\"1\",\"payload\":{}}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.eventType").value("manual.mapping_proposal.accepted"))
        .andExpect(jsonPath("$.source").value("manual"));
  }

  @Test
  void rejectsUnsafeEventTypes() throws Exception {
    mockMvc.perform(post("/api/v1/audit-events")
            .header("X-Api-Key", "00000000-0000-0000-0000-000000000a01")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"eventType\":\"<script>\",\"resourceType\":\"note\",\"resourceId\":\"1\",\"payload\":{}}"))
        .andExpect(status().isBadRequest());
  }
}
