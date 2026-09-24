package os.assurance.eu.api.corpus;

import java.time.LocalDate;
import java.util.List;

public final class CorpusCatalog {
  private CorpusCatalog() {
  }

  public static List<InstrumentSeed> instruments() {
    return List.of(
        new InstrumentSeed(
            "32024R1689",
            "02024R1689-20260727",
            LocalDate.of(2026, 7, 27),
            LocalDate.of(2026, 8, 2),
            "Artificial Intelligence Act",
            "corpus/formex/32024R1689.xml"),
        new InstrumentSeed(
            "32026R1744",
            null,
            null,
            LocalDate.of(2026, 7, 27),
            "Regulation (EU) 2026/1744 amending the Artificial Intelligence Act",
            "corpus/formex/32026R1744.xml"),
        new InstrumentSeed(
            "32016R0679",
            "02016R0679-20160504",
            LocalDate.of(2016, 5, 4),
            LocalDate.of(2018, 5, 25),
            "General Data Protection Regulation",
            "corpus/formex/32016R0679.xml"),
        new InstrumentSeed(
            "32022R2554",
            "02022R2554-20221227",
            LocalDate.of(2022, 12, 27),
            LocalDate.of(2025, 1, 17),
            "Digital Operational Resilience Act",
            "corpus/formex/32022R2554.xml"),
        new InstrumentSeed(
            "32022R2065",
            "02022R2065-20221027",
            LocalDate.of(2022, 10, 27),
            LocalDate.of(2024, 2, 17),
            "Digital Services Act",
            "corpus/formex/32022R2065.xml"),
        new InstrumentSeed(
            "32023R2854",
            "02023R2854-20231222",
            LocalDate.of(2023, 12, 22),
            LocalDate.of(2025, 9, 12),
            "Data Act",
            "corpus/formex/32023R2854.xml"));
  }

  public static List<GuidanceSeed> guidance() {
    return List.of(new GuidanceSeed(
        "edpb-gdpr-guidelines",
        "EDPB guidelines interpreting the GDPR",
        "EDPB",
        "interprets",
        "02016R0679-20160504#5:1:",
        "corpus/guidance/edpb-gdpr-guidelines.txt"));
  }
}
