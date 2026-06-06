package os.assurance.eu.api.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {
    @Id private UUID id;
    @Column(name = "key_hash", nullable = false, unique = true) private String keyHash;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected ApiKeyEntity() {}

    public ApiKeyEntity(UUID id, String keyHash, UUID tenantId, UUID userId, Instant createdAt) {
        this.id = id;
        this.keyHash = keyHash;
        this.tenantId = tenantId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public String keyHash() { return keyHash; }
    public UUID tenantId() { return tenantId; }
    public UUID userId() { return userId; }
}
