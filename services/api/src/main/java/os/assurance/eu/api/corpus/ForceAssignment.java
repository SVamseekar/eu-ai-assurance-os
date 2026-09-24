package os.assurance.eu.api.corpus;

import java.time.LocalDate;

public record ForceAssignment(ForceStatus status, LocalDate forceFrom, String scopeNote) {
}
