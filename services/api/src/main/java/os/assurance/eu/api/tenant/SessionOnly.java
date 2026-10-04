package os.assurance.eu.api.tenant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Guard for operations that mint or revoke credentials or grant access (API keys, invites, tenants). A leaked
 * API key must not be able to create replacements, add users, or undo other keys; those need a signed-in session.
 */
public final class SessionOnly {
  private SessionOnly() {
  }

  public static void require(HttpServletRequest request) {
    String apiKey = request.getHeader(TenantContextFilter.API_KEY_HEADER);
    if (apiKey != null && !apiKey.isBlank()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action needs a signed-in session, not an API key");
    }
  }
}
