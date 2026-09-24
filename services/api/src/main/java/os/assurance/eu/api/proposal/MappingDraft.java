package os.assurance.eu.api.proposal;

import java.time.LocalDate;

public record MappingDraft(
    String relation,
    String provisionKey,
    String forceStatus,
    LocalDate forceFrom,
    String excerpt) {

  public static MappingDraft abstain(String excerpt) {
    return new MappingDraft("abstain", null, null, null, excerpt);
  }
}
