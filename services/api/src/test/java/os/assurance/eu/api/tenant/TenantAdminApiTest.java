package os.assurance.eu.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import os.assurance.eu.api.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
@AutoConfigureMockMvc
class TenantAdminApiTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired JwtService jwtService;

  @Test
  void operatorAdminCanProvisionTenantInviteAndAccept() throws Exception {
    String unique = Long.toHexString(System.nanoTime());
    String adminEmail = "admin-" + unique + "@customer.example";
    MvcResult created = mockMvc.perform(post("/api/v1/admin/tenants")
            .with(operatorAdmin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Harborline Credit",
                  "plan": "design-partner",
                  "dataRegion": "EU",
                  "adminEmail": "%s",
                  "adminPassword": "customer-admin-pass"
                }
                """.formatted(adminEmail)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tenant.name").value("Harborline Credit"))
        .andExpect(jsonPath("$.admin.email").value(adminEmail))
        .andExpect(jsonPath("$.admin.role").value("ADMIN"))
        .andReturn();

    JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
    String tenantId = body.get("tenant").get("id").asText();
    String adminId = body.get("admin").get("id").asText();

    String engineerEmail = "eng-" + unique + "@customer.example";
    MvcResult invite = mockMvc.perform(post("/api/v1/admin/users/invites")
            .with(bearer(adminId, tenantId, UserRole.ADMIN))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "%s",
                  "role": "AI_ENGINEERING_LEAD"
                }
                """.formatted(engineerEmail)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(engineerEmail))
        .andExpect(jsonPath("$.inviteToken").isNotEmpty())
        .andReturn();

    String token = objectMapper.readTree(invite.getResponse().getContentAsString())
        .get("inviteToken").asText();

    mockMvc.perform(get("/auth/invites/{token}", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(engineerEmail))
        .andExpect(jsonPath("$.expired").value(false))
        .andExpect(jsonPath("$.accepted").value(false));

    mockMvc.perform(post("/auth/accept-invite")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "token": "%s",
                  "password": "engineer-pass-1"
                }
                """.formatted(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty());

    mockMvc.perform(get("/api/v1/admin/users").with(bearer(adminId, tenantId, UserRole.ADMIN)))
        .andExpect(status().isOk())
        .andExpect(result -> {
          String json = result.getResponse().getContentAsString();
          assertThat(json).contains(adminEmail);
          assertThat(json).contains(engineerEmail);
        });
  }

  @Test
  void nonOperatorCannotProvisionTenant() throws Exception {
    mockMvc.perform(post("/api/v1/admin/tenants")
            .with(complianceOfficer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Nope",
                  "adminEmail": "nope@example.com",
                  "adminPassword": "customer-admin-pass"
                }
                """))
        .andExpect(status().isForbidden());
  }

  private RequestPostProcessor operatorAdmin() {
    return bearer(
        TenantContext.DEFAULT_USER_ID.toString().replace("000000000101", "000000000105"),
        TenantContext.DEFAULT_TENANT_ID.toString(),
        UserRole.ADMIN);
  }

  private RequestPostProcessor complianceOfficer() {
    return bearer(
        TenantContext.DEFAULT_USER_ID.toString(),
        TenantContext.DEFAULT_TENANT_ID.toString(),
        UserRole.COMPLIANCE_OFFICER);
  }

  private RequestPostProcessor bearer(String userId, String tenantId, UserRole role) {
    String token = jwtService.issueAccessToken(
        java.util.UUID.fromString(userId),
        java.util.UUID.fromString(tenantId),
        role);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }
}
