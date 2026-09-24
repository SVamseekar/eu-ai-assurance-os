package os.assurance.eu.api.proposal;

import java.time.LocalDate;

public record LawProvision(
    String provisionKey,
    String text,
    String forceStatus,
    LocalDate forceFrom) {
}
