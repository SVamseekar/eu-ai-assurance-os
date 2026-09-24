package os.assurance.eu.api.corpus;

import java.time.LocalDate;

public record InstrumentSeed(
    String seedCelex,
    String consolidationCelex,
    LocalDate consolidationDate,
    LocalDate applicationFrom,
    String title,
    String formexResource) {
}
