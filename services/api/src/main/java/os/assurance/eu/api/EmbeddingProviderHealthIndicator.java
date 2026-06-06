package os.assurance.eu.api;

import os.assurance.eu.api.evidence.EvidenceEmbeddingProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("embeddingProvider")
public class EmbeddingProviderHealthIndicator implements HealthIndicator {
    private final EvidenceEmbeddingProvider provider;

    public EmbeddingProviderHealthIndicator(EvidenceEmbeddingProvider provider) {
        this.provider = provider;
    }

    @Override
    public Health health() {
        try {
            String probe = provider.embed("health check");
            if (probe == null || probe.isBlank()) {
                return Health.down().withDetail("reason", "embed returned empty").build();
            }
            return Health.up().withDetail("provider", provider.name()).build();
        } catch (Exception e) {
            return Health.down().withDetail("provider", provider.name()).build();
        }
    }
}
