package os.assurance.eu.api.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.auth.TokenResponse;
import os.assurance.eu.api.tenant.UserRole;

@RestController
public class DemoController {
  private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;

  private final JwtService jwtService;
  private final boolean enabled;

  public DemoController(JwtService jwtService, @Value("${assurance.demo.enabled:false}") boolean enabled) {
    this.jwtService = jwtService;
    this.enabled = enabled;
  }

  /** Short-lived access token for the read-only demo viewer. No refresh token is issued. */
  @PostMapping("/auth/demo")
  public TokenResponse demo() {
    if (!enabled) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    String access = jwtService.issueAccessToken(DemoProperties.DEMO_USER_ID, DemoProperties.DEMO_TENANT_ID, UserRole.AUDITOR);
    return new TokenResponse(access, "", ACCESS_TOKEN_TTL_SECONDS);
  }
}
