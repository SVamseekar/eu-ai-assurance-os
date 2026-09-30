package os.assurance.eu.api.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import os.assurance.eu.api.tenant.UserRole;

@Service
public class JwtService {
    private static final String ISSUER = "eu-ai-assurance-os";
    private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;

    private final SigningKeyJpaRepository signingKeys;
    private final String keyEncryptionSecret;
    private RSAKey activeSigningKey;

    public JwtService(
            SigningKeyJpaRepository signingKeys,
            @org.springframework.beans.factory.annotation.Value("${assurance.auth.key-encryption-secret:}") String keyEncryptionSecret) {
        this.signingKeys = signingKeys;
        this.keyEncryptionSecret = keyEncryptionSecret;
    }

    @PostConstruct
    void loadOrCreateActiveKey() {
        SigningKeyEntity entity = signingKeys.findByActiveTrue().orElseGet(this::generateAndPersistKey);
        if (keyEncryptionSecret != null && !keyEncryptionSecret.isBlank() && !KeyEncryption.isEncrypted(entity.privateKeyPem())) {
            entity.setPrivateKeyPem(KeyEncryption.encrypt(entity.privateKeyPem(), keyEncryptionSecret));
            entity = signingKeys.save(entity);
        }
        this.activeSigningKey = toRsaKey(entity);
    }

    private SigningKeyEntity generateAndPersistKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            String publicPem = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privatePem = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            String storedPrivatePem = (keyEncryptionSecret == null || keyEncryptionSecret.isBlank())
                ? privatePem
                : KeyEncryption.encrypt(privatePem, keyEncryptionSecret);
            SigningKeyEntity entity = new SigningKeyEntity(
                UUID.randomUUID(), "RS256", publicPem, storedPrivatePem, Instant.now(), true);
            return signingKeys.save(entity);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA key generation not available", e);
        }
    }

    private RSAKey toRsaKey(SigningKeyEntity entity) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(entity.publicKeyPem())));
            String decryptedPem = KeyEncryption.decrypt(entity.privateKeyPem(), keyEncryptionSecret);
            RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(decryptedPem)));
            return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(entity.kid().toString())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA signing key", e);
        }
    }

    public String issueAccessToken(UUID userId, UUID tenantId, UserRole role) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .claim("tenant_id", tenantId.toString())
                .claim("role", role.name())
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ACCESS_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS)))
                .build();
            SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(activeSigningKey.getKeyID()).build(),
                claims);
            jwt.sign(new RSASSASigner(activeSigningKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign access token", e);
        }
    }

    public record AccessTokenClaims(UUID userId, UUID tenantId, UserRole role) {}

    public Optional<AccessTokenClaims> verifyAccessToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new RSASSAVerifier(activeSigningKey.toRSAPublicKey()))) {
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                return Optional.empty();
            }
            if (!ISSUER.equals(claims.getIssuer())) {
                return Optional.empty();
            }
            UUID userId = UUID.fromString(claims.getSubject());
            UUID tenantId = UUID.fromString(claims.getStringClaim("tenant_id"));
            UserRole role = UserRole.valueOf(claims.getStringClaim("role"));
            return Optional.of(new AccessTokenClaims(userId, tenantId, role));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public JWKSet currentPublicJwks() {
        return new JWKSet(List.of(activeSigningKey.toPublicJWK()));
    }
}
