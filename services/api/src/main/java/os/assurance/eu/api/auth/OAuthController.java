package os.assurance.eu.api.auth;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.observability.AssuranceMetrics;

/**
 * Server-side OAuth authorization-code endpoints (Google + Microsoft).
 *
 * <ul>
 *   <li>{@code GET /auth/oauth/{provider}/start} — 302 to the IdP authorize URL</li>
 *   <li>{@code POST /auth/oauth/{provider}/callback} — exchange code+state for JWT pair</li>
 * </ul>
 *
 * <p>The browser-facing callback is handled by the Next.js BFF which POSTs here and sets
 * the same httpOnly session cookies as password login.
 */
@RestController
public class OAuthController {
  private final OAuthService oauthService;
  private final OAuthProperties properties;
  private final AssuranceMetrics assuranceMetrics;

  public OAuthController(
      OAuthService oauthService,
      OAuthProperties properties,
      AssuranceMetrics assuranceMetrics) {
    this.oauthService = oauthService;
    this.properties = properties;
    this.assuranceMetrics = assuranceMetrics;
  }

  @GetMapping("/auth/oauth/{provider}/start")
  public ResponseEntity<Void> start(@PathVariable String provider) {
    try {
      String authorizationUrl = oauthService.beginAuthorization(provider);
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(authorizationUrl))
          .build();
    } catch (OAuthService.OAuthLoginException e) {
      assuranceMetrics.authLoginFailure("oauth_" + e.code());
      return redirectToLoginError(e.code());
    } catch (OAuthTokenClient.OAuthExchangeException e) {
      assuranceMetrics.authLoginFailure("oauth_" + e.code());
      String code = "not_configured".equals(e.code()) || "unsupported_provider".equals(e.code())
          ? e.code()
          : "sign_in_unavailable";
      return redirectToLoginError(code);
    }
  }

  /**
   * Token exchange used by the Next.js callback BFF. Returns the same {@link TokenResponse}
   * shape as {@code POST /auth/login}.
   */
  @PostMapping("/auth/oauth/{provider}/callback")
  public TokenResponse callback(
      @PathVariable String provider,
      @RequestBody OAuthCallbackRequest request) {
    try {
      return oauthService.completeAuthorization(
          provider,
          request == null ? null : request.code(),
          request == null ? null : request.state());
    } catch (OAuthService.OAuthLoginException e) {
      assuranceMetrics.authLoginFailure("oauth_" + e.code());
      throw toStatus(e);
    } catch (OAuthTokenClient.OAuthExchangeException e) {
      assuranceMetrics.authLoginFailure("oauth_" + e.code());
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
    } catch (IllegalArgumentException e) {
      assuranceMetrics.authLoginFailure("oauth_profile");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  private ResponseEntity<Void> redirectToLoginError(String authError) {
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(loginBase() + "?auth_error=" + authError))
        .build();
  }

  private String loginBase() {
    String base = properties.getRedirectBaseUrl();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/login";
  }

  private static ResponseStatusException toStatus(OAuthService.OAuthLoginException e) {
    return switch (e.code()) {
      case "not_provisioned" -> new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
      case "state" -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
      case "denied" -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
      case "unsupported_provider" -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
      default -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    };
  }

  public record OAuthCallbackRequest(String code, String state) {}
}
