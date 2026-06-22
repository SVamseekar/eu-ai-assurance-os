package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import os.assurance.eu.api.tenant.TenantContext;

@SpringBootTest
@Transactional
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Test
    void issuesARefreshTokenThatRotatesSuccessfully() {
        UUID userId = TenantContext.DEFAULT_USER_ID;
        UUID tenantId = TenantContext.DEFAULT_TENANT_ID;

        var issued = refreshTokenService.issue(userId, tenantId);
        var result = refreshTokenService.rotate(issued.rawToken());

        assertThat(result).isInstanceOf(RefreshTokenService.RefreshResult.Rotated.class);
        var rotated = (RefreshTokenService.RefreshResult.Rotated) result;
        assertThat(rotated.userId()).isEqualTo(userId);
        assertThat(rotated.tenantId()).isEqualTo(tenantId);
        assertThat(rotated.newToken().rawToken()).isNotEqualTo(issued.rawToken());
    }

    @Test
    void rejectsAnUnknownToken() {
        var result = refreshTokenService.rotate("this-token-was-never-issued");

        assertThat(result).isInstanceOf(RefreshTokenService.RefreshResult.Rejected.class);
    }

    @Test
    void revokesTheWholeChainWhenAnAlreadyRotatedTokenIsReused() {
        UUID userId = TenantContext.DEFAULT_USER_ID;
        UUID tenantId = TenantContext.DEFAULT_TENANT_ID;

        var firstIssued = refreshTokenService.issue(userId, tenantId);
        var firstRotation = (RefreshTokenService.RefreshResult.Rotated) refreshTokenService.rotate(firstIssued.rawToken());

        // Reuse the already-rotated (now revoked) first token — simulates token theft.
        var reuseAttempt = refreshTokenService.rotate(firstIssued.rawToken());
        assertThat(reuseAttempt).isInstanceOf(RefreshTokenService.RefreshResult.Rejected.class);

        // The legitimately-rotated second token must now ALSO be rejected — the whole chain is burned.
        var secondTokenNowRejected = refreshTokenService.rotate(firstRotation.newToken().rawToken());
        assertThat(secondTokenNowRejected).isInstanceOf(RefreshTokenService.RefreshResult.Rejected.class);
    }

    @Test
    void logoutRevokesTheToken() {
        var issued = refreshTokenService.issue(TenantContext.DEFAULT_USER_ID, TenantContext.DEFAULT_TENANT_ID);

        refreshTokenService.revoke(issued.rawToken());
        var result = refreshTokenService.rotate(issued.rawToken());

        assertThat(result).isInstanceOf(RefreshTokenService.RefreshResult.Rejected.class);
    }
}