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
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import os.assurance.eu.api.email.EmailMessage;
import os.assurance.eu.api.email.EmailSender;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
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
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
  @MockitoSpyBean EmailSender emailSender;

  private String inviteTokenEmailedTo(String to) {
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getAllValues().stream().filter(m -> to.equalsIgnoreCase(m.to()))
        .reduce((a, b) -> b).orElseThrow().textBody();
    return body.substring(body.indexOf("token=") + 6).split("\\s")[0];
  }

  private String signupAdmin(String email) throws Exception {
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"organisationName\":\"Invite Org\"}"))
        .andExpect(status().isAccepted());
    String token = inviteTokenEmailedTo(email);
    MvcResult verified = mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"a-long-password-1\"}"))
        .andExpect(status().isOk()).andReturn();
    return objectMapper.readTree(verified.getResponse().getContentAsString()).get("accessToken").asText();
  }

  @Test
  void inviteTokenIsEmailedToInviteeAndNeverReturnedToInviter() throws Exception {
    String unique = Long.toHexString(System.nanoTime());
    String admin = signupAdmin("owner-" + unique + "@invite.example");
    String victim = "ceo-" + unique + "@victim.example";
    mockMvc.perform(post("/api/v1/admin/users/invites")
            .header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + victim + "\",\"role\":\"AUDITOR\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.inviteToken").doesNotExist())
        .andExpect(jsonPath("$.acceptPath").value("/invite"));
    String token = inviteTokenEmailedTo(victim);
    mockMvc.perform(get("/auth/invites/{token}", token)).andExpect(status().isOk());
  }

  @Test
  void invitingARegisteredAddressLooksIdenticalAndSendsNothing() throws Exception {
    String unique = Long.toHexString(System.nanoTime());
    String admin = signupAdmin("owner2-" + unique + "@invite.example");
    String existing = "taken-" + unique + "@invite.example";
    signupAdmin(existing);
    org.mockito.Mockito.clearInvocations(emailSender);
    mockMvc.perform(post("/api/v1/admin/users/invites")
            .header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + existing + "\",\"role\":\"AUDITOR\"}"))
        .andExpect(status().isCreated());
    verify(emailSender, never()).send(any());
  }

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

    // The new tenant's own audit trail must not point at users of another tenant, or purging the
    // operator tenant would be blocked by a foreign key.
    assertThat(jdbc.queryForObject(
        "select count(*) from audit_events a join users u on u.id = a.actor_id "
            + "where a.tenant_id = ? and u.tenant_id <> a.tenant_id",
        Integer.class, java.util.UUID.fromString(tenantId))).isZero();

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
        .andReturn();

    String token = inviteTokenEmailedTo(engineerEmail);

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
