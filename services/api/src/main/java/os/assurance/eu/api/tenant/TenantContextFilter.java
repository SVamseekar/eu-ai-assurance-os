package os.assurance.eu.api.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantContextFilter extends OncePerRequestFilter {
  private final TenantJpaRepository tenants;
  private final UserJpaRepository users;

  public TenantContextFilter(TenantJpaRepository tenants, UserJpaRepository users) {
    this.tenants = tenants;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    UUID tenantId = parseHeader(request, TenantContext.TENANT_HEADER, TenantContext.DEFAULT_TENANT_ID, response);
    if (tenantId == null) {
      return;
    }
    UUID actorId = parseHeader(request, TenantContext.ACTOR_HEADER, TenantContext.DEFAULT_USER_ID, response);
    if (actorId == null) {
      return;
    }
    if (!tenants.existsById(tenantId)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown tenant");
      return;
    }
    if (!users.existsByIdAndTenantId(actorId, tenantId)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown actor for tenant");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private UUID parseHeader(
      HttpServletRequest request,
      String headerName,
      UUID fallback,
      HttpServletResponse response) throws IOException {
    String value = request.getHeader(headerName);
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + headerName);
      return null;
    }
  }
}
