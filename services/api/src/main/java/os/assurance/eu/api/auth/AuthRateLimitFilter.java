package os.assurance.eu.api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class AuthRateLimitFilter extends OncePerRequestFilter {
  static final String CLIENT_IP_HEADER = "X-Client-IP";
  // /auth/refresh is not IP-limited: it needs a 256-bit token, and all BFF traffic shares one IP.
  static final List<String> LIMITED_PREFIXES = List.of(
      "/auth/login", "/auth/accept-invite", "/auth/oauth/",
      "/auth/signup", "/auth/password/", "/auth/verify-email", "/auth/demo", "/api/public/");

  private final SlidingWindowRateLimiter perIp;
  private final boolean trustClientIpHeader;

  public AuthRateLimitFilter(
      @Value("${assurance.security.auth-rate.per-ip-per-15m:30}") int perIpLimit,
      @Value("${assurance.security.trust-client-ip-header:false}") boolean trustClientIpHeader,
      Clock clock) {
    this.perIp = new SlidingWindowRateLimiter(perIpLimit, Duration.ofMinutes(15), clock);
    this.trustClientIpHeader = trustClientIpHeader;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String uri = request.getRequestURI();
    return LIMITED_PREFIXES.stream().noneMatch(uri::startsWith);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!perIp.tryAcquire(clientIp(request))) {
      tooMany(response);
      return;
    }
    chain.doFilter(request, response);
  }

  String clientIp(HttpServletRequest request) {
    if (trustClientIpHeader) {
      String forwarded = request.getHeader(CLIENT_IP_HEADER);
      if (forwarded != null && !forwarded.isBlank()) {
        return forwarded.trim();
      }
    }
    return request.getRemoteAddr();
  }

  static void tooMany(HttpServletResponse response) throws IOException {
    response.setStatus(429);
    response.setHeader("Retry-After", "900");
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"too_many_requests\"}");
  }
}
