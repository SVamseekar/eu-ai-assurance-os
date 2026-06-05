package os.assurance.eu.api.system;

import java.time.Instant;
import java.util.UUID;

public record CreateAiSystemResponse(UUID id, ReleaseDecision releaseDecision, Instant createdAt) {
}
