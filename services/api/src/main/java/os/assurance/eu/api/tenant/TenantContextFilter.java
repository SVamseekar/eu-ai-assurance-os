package os.assurance.eu.api.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import os.assurance.eu.api.auth.JwtService;

@Component
public class TenantContextFilter extends OncePerRequestFilter {
    static final String API_KEY_HEADER = "X-Api-Key";
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String BEARER_PREFIX = "Bearer ";

    private static final Set<String> UNAUTHENTICATED_PATHS = Set.of(
        "/.well-known/jwks.json",
        "/auth/login",
        "/auth/refresh",
        "/auth/logout",
        "/actuator/health");

    private final ApiKeyJpaRepository apiKeys;
    private final TenantContext tenantContext;
    private final JwtService jwtService;

    public TenantContextFilter(
            ApiKeyJpaRepository apiKeys,
            TenantContext tenantContext,
            JwtService jwtService) {
        this.apiKeys = apiKeys;
        this.tenantContext = tenantContext;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        if (UNAUTHENTICATED_PATHS.contains(requestUri) || requestUri.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKeyHeader = request.getHeader(API_KEY_HEADER);
        if (apiKeyHeader != null && !apiKeyHeader.isBlank()) {
            authenticateWithApiKey(apiKeyHeader, request, response, filterChain);
            return;
        }

        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            authenticateWithBearerToken(authorizationHeader.substring(BEARER_PREFIX.length()), request, response, filterChain);
            return;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or unsupported authentication");
    }

    private void authenticateWithApiKey(
            String apiKeyHeader,
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            UUID.fromString(apiKeyHeader);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + API_KEY_HEADER);
            return;
        }
        String keyHash = ApiKeyHasher.sha256Hex(apiKeyHeader);
        ApiKeyEntity key = apiKeys.findByKeyHash(keyHash).orElse(null);
        if (key == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown API key");
            return;
        }
        tenantContext.setOverrides(key.tenantId(), key.userId());
        try {
            filterChain.doFilter(request, response);
        } finally {
            tenantContext.clearOverrides();
        }
    }

    private void authenticateWithBearerToken(
            String token,
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        var claims = jwtService.verifyAccessToken(token);
        if (claims.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired access token");
            return;
        }
        tenantContext.setOverrides(claims.get().tenantId(), claims.get().userId());
        try {
            filterChain.doFilter(request, response);
        } finally {
            tenantContext.clearOverrides();
        }
    }
}