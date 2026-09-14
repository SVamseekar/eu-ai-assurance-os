package os.assurance.eu.api.ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import os.assurance.eu.api.auth.OAuthProperties;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ops")
public class OpsReadinessController {
  private final TenantAuthorizationService authorization;
  private final OAuthProperties oauthProperties;
  private final Environment environment;
  private final String evalCallbackSecret;
  private final boolean publicClaimsSeeding;

  public OpsReadinessController(
      TenantAuthorizationService authorization,
      OAuthProperties oauthProperties,
      Environment environment,
      @Value("${assurance.eval.callback.secret:}") String evalCallbackSecret,
      @Value("${assurance.demo.public-claims:false}") boolean publicClaimsSeeding) {
    this.authorization = authorization;
    this.oauthProperties = oauthProperties;
    this.environment = environment;
    this.evalCallbackSecret = evalCallbackSecret == null ? "" : evalCallbackSecret;
    this.publicClaimsSeeding = publicClaimsSeeding;
  }

  @GetMapping("/readiness")
  public OpsReadinessResponse readiness() {
    authorization.requireAnyRole(UserRole.ADMIN);
    boolean postgres = Arrays.asList(environment.getActiveProfiles()).contains("postgres");
    boolean evalSecret = !evalCallbackSecret.isBlank();
    boolean google = oauthProperties.getGoogle() != null
        && oauthProperties.getGoogle().getClientId() != null
        && !oauthProperties.getGoogle().getClientId().isBlank();
    boolean microsoft = oauthProperties.getMicrosoft() != null
        && oauthProperties.getMicrosoft().getClientId() != null
        && !oauthProperties.getMicrosoft().getClientId().isBlank();
    boolean oauthSmoke = google || microsoft;
    boolean autoProvisionOff = !oauthProperties.isAutoProvision();

    Map<String, Boolean> checks = new LinkedHashMap<>();
    checks.put("postgresProfile", postgres);
    checks.put("evalCallbackSecretSet", evalSecret);
    checks.put("oauthGoogleConfigured", google);
    checks.put("oauthMicrosoftConfigured", microsoft);
    checks.put("oauthProviderConfigured", oauthSmoke);
    checks.put("oauthAutoProvisionDisabled", autoProvisionOff);
    checks.put("publicClaimsSeedingDisabled", !publicClaimsSeeding);

    List<String> blockers = new ArrayList<>();
    if (!postgres) {
      blockers.add("API is not on the postgres profile (H2 is not a production store).");
    }
    if (postgres && publicClaimsSeeding) {
      blockers.add("ASSURANCE_PUBLIC_CLAIMS is on — named public-claims teasers must not seed a paying tenant.");
    }
    if (!evalSecret) {
      blockers.add("EVAL_CALLBACK_SECRET is empty.");
    }
    if (!autoProvisionOff) {
      blockers.add("OAUTH_AUTO_PROVISION is enabled — unknown accounts can self-join.");
    }

    boolean productionReady = blockers.isEmpty();
    return new OpsReadinessResponse(
        "Operator checklist only. Not an SLA, SOC 2 report, pentest, or certification.",
        productionReady,
        checks,
        blockers);
  }
}
