package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
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
class EvidencePackSignatureTest {
  private static final String KEY = "00000000-0000-0000-0000-000000000a01";
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void packSignatureVerifiesAgainstPublishedJwksAndBindsTheContentHash() throws Exception {
    String created = mockMvc.perform(post("/api/v1/systems").header("X-Api-Key", KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Signed pack\",\"owner\":\"Ops\",\"purpose\":\"Test\",\"riskClass\":\"LIMITED\",\"riskBasis\":\"Art 50\",\"deploymentRegion\":\"EU\"}"))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String id = objectMapper.readTree(created).get("id").asText();

    JsonNode pack = objectMapper.readTree(mockMvc.perform(get("/api/v1/systems/" + id + "/evidence-pack").header("X-Api-Key", KEY))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    String jwks = mockMvc.perform(get("/.well-known/jwks.json")).andReturn().getResponse().getContentAsString();

    JWSObject jws = JWSObject.parse(pack.get("signature").asText());
    RSAKey key = (RSAKey) JWKSet.parse(jwks).getKeyByKeyId(jws.getHeader().getKeyID());
    assertThat(jws.verify(new RSASSAVerifier(key))).isTrue();
    assertThat(jws.getPayload().toJSONObject().get("contentSha256")).isEqualTo(pack.get("contentSha256").asText());
    assertThat(jws.getPayload().toJSONObject().get("systemId")).isEqualTo(id);
  }
}
