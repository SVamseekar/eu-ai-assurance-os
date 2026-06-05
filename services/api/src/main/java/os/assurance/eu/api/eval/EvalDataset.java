package os.assurance.eu.api.eval;

import java.time.Instant;
import java.util.UUID;

public record EvalDataset(
    UUID id,
    String name,
    String version,
    int sampleCount,
    boolean golden,
    Instant createdAt) {
}
