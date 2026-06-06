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
    static final String API_KEY_HEADER = "X-Api-Key";

    private final TenantJpaRepository tenants;
    private final UserJpaRepository users;
    private final ApiKeyJpaRepository apiKeys;
    private final TenantContext tenantContext;

    public TenantContextFilter(
            TenantJpaRepository tenants,
            UserJpaRepository users,
            ApiKeyJpaRepository apiKeys,
            TenantContext tenantContext) {
        this.tenants = tenants;
        this.users = users;
        this.apiKeys = apiKeys;
        this.tenantContext = tenantContext;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKeyHeader = request.getHeader(API_KEY_HEADER);

        if (apiKeyHeader != null && !apiKeyHeader.isBlank()) {
            UUID keyId;
            try {
                keyId = UUID.fromString(apiKeyHeader);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + API_KEY_HEADER);
                return;
            }
            ApiKeyEntity key = apiKeys.findById(keyId).orElse(null);
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
            return;
        }

        // Legacy header fallback (for existing tests and dev tooling)
        UUID tenantId = parseHeader(request, TenantContext.TENANT_HEADER, TenantContext.DEFAULT_TENANT_ID, response);
        if (tenantId == null) return;
        UUID actorId = parseHeader(request, TenantContext.ACTOR_HEADER, TenantContext.DEFAULT_USER_ID, response);
        if (actorId == null) return;

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
        if (value == null || value.isBlank()) return fallback;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + headerName);
            return null;
        }
    }
}
