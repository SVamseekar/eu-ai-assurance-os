package os.assurance.eu.api.tenant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.auth.RefreshTokenService;
import os.assurance.eu.api.auth.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantAdminService {
  private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final TenantJpaRepository tenants;
  private final UserJpaRepository users;
  private final UserInviteJpaRepository invites;
  private final TenantContext tenantContext;
  private final TenantAuthorizationService authorization;
  private final PlatformProperties platformProperties;
  private final AuditService auditService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final Clock clock;
  private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder(12);

  public TenantAdminService(
      TenantJpaRepository tenants,
      UserJpaRepository users,
      UserInviteJpaRepository invites,
      TenantContext tenantContext,
      TenantAuthorizationService authorization,
      PlatformProperties platformProperties,
      AuditService auditService,
      JwtService jwtService,
      RefreshTokenService refreshTokenService,
      Clock clock) {
    this.tenants = tenants;
    this.users = users;
    this.invites = invites;
    this.tenantContext = tenantContext;
    this.authorization = authorization;
    this.platformProperties = platformProperties;
    this.auditService = auditService;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.clock = clock;
  }

  @Transactional
  public CreateTenantResponse createTenant(CreateTenantRequest request) {
    requireOperatorAdmin();
    String email = normalizeEmail(request.adminEmail());
    if (users.existsByEmailIgnoreCase(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }
    Instant now = clock.instant();
    UUID tenantId = UUID.randomUUID();
    String plan = blankTo(request.plan(), "design-partner");
    String region = blankTo(request.dataRegion(), "EU");
    TenantEntity tenant = tenants.save(new TenantEntity(tenantId, request.name().trim(), plan, region, now));
    UUID adminId = UUID.randomUUID();
    UserEntity admin = users.save(new UserEntity(
        adminId,
        tenantId,
        email,
        UserRole.ADMIN,
        passwords.encode(request.adminPassword()),
        now));
    tenantContext.withTenant(tenantId, () -> {
      auditService.append(
          null,
          "tenant.provisioned",
          "tenant",
          tenantId.toString(),
          java.util.Map.of(
              "name", tenant.name(),
              "adminEmail", email,
              "plan", plan,
              "dataRegion", region));
      return null;
    });
    return new CreateTenantResponse(TenantView.from(tenant), UserView.from(admin));
  }

  @Transactional(readOnly = true)
  public List<UserView> listUsers() {
    authorization.requireAnyRole(UserRole.ADMIN);
    return users.findAllByTenantIdOrderByCreatedAtAsc(tenantContext.tenantId()).stream()
        .map(UserView::from)
        .toList();
  }

  @Transactional
  public InviteCreatedResponse inviteUser(InviteUserRequest request) {
    authorization.requireAnyRole(UserRole.ADMIN);
    String email = normalizeEmail(request.email());
    if (users.existsByEmailIgnoreCase(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }
    Instant now = clock.instant();
    String rawToken = newToken();
    UserInviteEntity invite = invites.save(new UserInviteEntity(
        UUID.randomUUID(),
        tenantContext.tenantId(),
        email,
        request.role(),
        sha256Hex(rawToken),
        tenantContext.actorId(),
        now.plus(platformProperties.getInviteTtlHours(), ChronoUnit.HOURS),
        now));
    auditService.append(
        null,
        "user.invited",
        "user_invite",
        invite.id().toString(),
        java.util.Map.of("email", email, "role", request.role().name()));
    return new InviteCreatedResponse(
        invite.id(),
        invite.tenantId(),
        invite.email(),
        invite.role(),
        invite.expiresAt(),
        rawToken,
        "/invite?token=" + rawToken);
  }

  @Transactional(readOnly = true)
  public List<InviteCreatedResponse> listInvites() {
    authorization.requireAnyRole(UserRole.ADMIN);
    return invites.findAllByTenantIdOrderByCreatedAtDesc(tenantContext.tenantId()).stream()
        .map(invite -> new InviteCreatedResponse(
            invite.id(),
            invite.tenantId(),
            invite.email(),
            invite.role(),
            invite.expiresAt(),
            null,
            "/invite"))
        .toList();
  }

  @Transactional(readOnly = true)
  public InvitePreview previewInvite(String rawToken) {
    UserInviteEntity invite = requireInvite(rawToken);
    TenantEntity tenant = tenants.findById(invite.tenantId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    Instant now = clock.instant();
    boolean expired = invite.expiresAt().isBefore(now);
    boolean accepted = invite.acceptedAt() != null;
    return new InvitePreview(
        invite.email(),
        invite.role(),
        tenant.name(),
        invite.expiresAt(),
        expired,
        accepted);
  }

  @Transactional
  public TokenResponse acceptInvite(AcceptInviteRequest request) {
    UserInviteEntity invite = requireInvite(request.token());
    Instant now = clock.instant();
    if (invite.acceptedAt() != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Invite already accepted");
    }
    if (invite.expiresAt().isBefore(now)) {
      throw new ResponseStatusException(HttpStatus.GONE, "Invite expired");
    }
    if (users.existsByEmailIgnoreCase(invite.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }
    UserEntity user = users.save(new UserEntity(
        UUID.randomUUID(),
        invite.tenantId(),
        invite.email(),
        invite.role(),
        passwords.encode(request.password()),
        now));
    invite.markAccepted(now);
    invites.save(invite);
    tenantContext.setOverrides(invite.tenantId(), user.id());
    try {
      auditService.append(
          null,
          "user.invite.accepted",
          "user",
          user.id().toString(),
          java.util.Map.of("email", user.email(), "role", user.role().name()));
    } finally {
      tenantContext.clearOverrides();
    }
    String access = jwtService.issueAccessToken(user.id(), user.tenantId(), user.role());
    var refresh = refreshTokenService.issue(user.id(), user.tenantId());
    return new TokenResponse(access, refresh.rawToken(), ACCESS_TOKEN_TTL_SECONDS);
  }

  private void requireOperatorAdmin() {
    authorization.requireAnyRole(UserRole.ADMIN);
    if (!platformProperties.getOperatorTenantId().equals(tenantContext.tenantId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the platform operator tenant can provision customer tenants");
    }
  }

  private UserInviteEntity requireInvite(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite token is required");
    }
    return invites.findByTokenHash(sha256Hex(rawToken.trim()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found"));
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private static String blankTo(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }

  private static String newToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }

  static String sha256Hex(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
