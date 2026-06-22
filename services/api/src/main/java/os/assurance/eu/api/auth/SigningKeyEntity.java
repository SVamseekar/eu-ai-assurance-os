package os.assurance.eu.api.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signing_keys")
public class SigningKeyEntity {
    @Id private UUID kid;
    @Column(nullable = false) private String algorithm;
    @Column(name = "public_key_pem", nullable = false) private String publicKeyPem;
    @Column(name = "private_key_pem", nullable = false) private String privateKeyPem;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(nullable = false) private boolean active;

    protected SigningKeyEntity() {}

    public SigningKeyEntity(UUID kid, String algorithm, String publicKeyPem, String privateKeyPem, Instant createdAt, boolean active) {
        this.kid = kid;
        this.algorithm = algorithm;
        this.publicKeyPem = publicKeyPem;
        this.privateKeyPem = privateKeyPem;
        this.createdAt = createdAt;
        this.active = active;
    }

    public UUID kid() { return kid; }
    public String algorithm() { return algorithm; }
    public String publicKeyPem() { return publicKeyPem; }
    public String privateKeyPem() { return privateKeyPem; }
    public Instant createdAt() { return createdAt; }
    public boolean active() { return active; }
}