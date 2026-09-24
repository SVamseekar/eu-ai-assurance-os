package os.assurance.eu.api.corpus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorpusIngestService {
  public static final String ATTRIBUTION =
      "EUR-Lex text is reused under Commission Decision 2011/833/EU. Guidance is stored as an interpretation, not as a statute.";

  private final CorpusVersionJpaRepository versions;
  private final InstrumentJpaRepository instruments;
  private final ProvisionJpaRepository provisions;
  private final GuidanceDocJpaRepository guidanceDocs;
  private final CorpusRelationshipJpaRepository relationships;
  private final FormexProvisionParser parser;
  private final ForceDateRules forceDateRules;
  private final LocalDate asOf;

  public CorpusIngestService(
      CorpusVersionJpaRepository versions,
      InstrumentJpaRepository instruments,
      ProvisionJpaRepository provisions,
      GuidanceDocJpaRepository guidanceDocs,
      CorpusRelationshipJpaRepository relationships,
      @Value("${assurance.corpus.as-of:}") String asOf) {
    this.versions = versions;
    this.instruments = instruments;
    this.provisions = provisions;
    this.guidanceDocs = guidanceDocs;
    this.relationships = relationships;
    this.parser = new FormexProvisionParser();
    this.forceDateRules = new ForceDateRules();
    this.asOf = asOf == null || asOf.isBlank() ? LocalDate.now() : LocalDate.parse(asOf);
  }

  @Transactional
  public String ingestClasspath() {
    List<LoadedInstrument> loaded = new ArrayList<>();
    List<CorpusVersionHasher.InstrumentLine> instrumentLines = new ArrayList<>();
    for (InstrumentSeed seed : CorpusCatalog.instruments()) {
      byte[] formex = read(seed.formexResource());
      String textHash = CorpusVersionHasher.sha256(formex);
      String textCelex = seed.consolidationCelex() == null ? seed.seedCelex() : seed.consolidationCelex();
      loaded.add(new LoadedInstrument(seed, textHash, parser.parse(textCelex, formex)));
      instrumentLines.add(new CorpusVersionHasher.InstrumentLine(
          seed.seedCelex(),
          seed.consolidationDate() == null ? "" : seed.consolidationDate().toString(),
          textHash,
          seed.applicationFrom().toString()));
    }
    List<LoadedGuidance> loadedGuidance = new ArrayList<>();
    List<CorpusVersionHasher.GuidanceLine> guidanceLines = new ArrayList<>();
    for (GuidanceSeed seed : CorpusCatalog.guidance()) {
      String body = new String(read(seed.bodyResource()), StandardCharsets.UTF_8);
      String textHash = CorpusVersionHasher.sha256(body);
      loadedGuidance.add(new LoadedGuidance(seed, textHash, truncate(body)));
      guidanceLines.add(new CorpusVersionHasher.GuidanceLine(seed.sourceKey(), textHash));
    }
    String versionHash = CorpusVersionHasher.hash(instrumentLines, guidanceLines);
    if (versions.findByVersionHash(versionHash).isPresent()) {
      return versionHash;
    }
    UUID versionId = UUID.randomUUID();
    versions.save(new CorpusVersionEntity(versionId, versionHash, Instant.now()));
    for (LoadedInstrument loadedInstrument : loaded) {
      InstrumentSeed seed = loadedInstrument.seed();
      UUID instrumentId = UUID.randomUUID();
      instruments.save(new InstrumentEntity(
          instrumentId,
          versionId,
          seed.seedCelex(),
          seed.consolidationCelex(),
          seed.title(),
          "ENG",
          loadedInstrument.textHash(),
          seed.consolidationDate(),
          seed.applicationFrom()));
      String textCelex = seed.consolidationCelex() == null ? seed.seedCelex() : seed.consolidationCelex();
      for (ParsedProvision parsed : loadedInstrument.provisions()) {
        ForceAssignment force = forceDateRules.assign(textCelex, parsed, asOf);
        provisions.save(new ProvisionEntity(
            UUID.randomUUID(),
            instrumentId,
            parsed.provisionKey(),
            emptyToNull(parsed.article()),
            emptyToNull(parsed.paragraph()),
            emptyToNull(parsed.point()),
            emptyToNull(parsed.annex()),
            truncate(parsed.text()),
            force.status(),
            force.forceFrom(),
            force.scopeNote()));
      }
    }
    for (LoadedGuidance document : loadedGuidance) {
      GuidanceSeed seed = document.seed();
      guidanceDocs.save(new GuidanceDocEntity(
          UUID.randomUUID(),
          versionId,
          seed.sourceKey(),
          seed.title(),
          seed.authorityRank(),
          document.textHash(),
          document.body()));
      relationships.save(new CorpusRelationshipEntity(
          UUID.randomUUID(),
          versionId,
          seed.relation(),
          "guidance",
          seed.sourceKey(),
          "provision",
          seed.interpretsProvisionKey()));
    }
    return versionHash;
  }

  private static byte[] read(String path) {
    try {
      return new ClassPathResource(path).getContentAsByteArray();
    } catch (IOException ex) {
      throw new IllegalStateException("Missing corpus resource " + path, ex);
    }
  }

  private static String truncate(String value) {
    if (value == null) {
      return "";
    }
    String trimmed = value.trim();
    return trimmed.length() <= 4000 ? trimmed : trimmed.substring(0, 4000);
  }

  private static String emptyToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private record LoadedInstrument(InstrumentSeed seed, String textHash, List<ParsedProvision> provisions) {
  }

  private record LoadedGuidance(GuidanceSeed seed, String textHash, String body) {
  }
}
