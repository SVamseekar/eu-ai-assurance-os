package os.assurance.eu.api.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import os.assurance.eu.api.tenant.TenantContext;

/**
 * Keeps the public demo workspace read-only. Runs after {@code TenantContextFilter} (order 2), which sets the
 * tenant for the duration of the chain, so the tenant is known here.
 */
@Component
@Order(3)
public class DemoReadOnlyFilter extends OncePerRequestFilter {
  private final TenantContext tenantContext;

  public DemoReadOnlyFilter(TenantContext tenantContext) {
    this.tenantContext = tenantContext;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String method = request.getMethod();
    boolean write = !("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method));
    boolean allowedWrite = "POST".equals(method) && "/api/v1/evidence/query".equals(request.getRequestURI());
    if (write && !allowedWrite && isDemoTenant()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "The demo workspace is read-only. Sign up to make changes.");
      return;
    }
    chain.doFilter(request, response);
  }

  private boolean isDemoTenant() {
    try {
      return DemoProperties.DEMO_TENANT_ID.equals(tenantContext.tenantId());
    } catch (IllegalStateException unauthenticated) {
      return false;
    }
  }
}
