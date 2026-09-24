package os.assurance.eu.api.corpus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorpusQueryService {
  private final CorpusVersionJpaRepository versions;
  private final InstrumentJpaRepository instruments;
  private final ProvisionJpaRepository provisions;
  private final GuidanceDocJpaRepository guidanceDocs;
  private final CorpusRelationshipJpaRepository relationships;

  public CorpusQueryService(
      CorpusVersionJpaRepository versions,
      InstrumentJpaRepository instruments,
      ProvisionJpaRepository provisions,
      GuidanceDocJpaRepository guidanceDocs,
      CorpusRelationshipJpaRepository relationships) {
    this.versions = versions;
    this.instruments = instruments;
    this.provisions = provisions;
    this.guidanceDocs = guidanceDocs;
    this.relationships = relationships;
  }

  @Transactional(readOnly = true)
  public CorpusView current() {
    return versions.findFirstByOrderByBuiltAtDesc()
        .map(this::view)
        .orElseGet(() -> new CorpusView(null, List.of(), List.of(), List.of(), CorpusIngestService.ATTRIBUTION));
  }

  @Transactional(readOnly = true)
  public String currentVersionHash() {
    return versions.findFirstByOrderByBuiltAtDesc().map(CorpusVersionEntity::versionHash).orElse("");
  }

  @Transactional(readOnly = true)
  public boolean currentCorpusContains(String provisionKey) {
    if (provisionKey == null || provisionKey.isBlank()) {
      return false;
    }
    CorpusVersionEntity version = versions.findFirstByOrderByBuiltAtDesc().orElse(null);
    if (version == null) {
      return false;
    }
    List<UUID> instrumentIds = instruments.findAllByCorpusVersionIdOrderBySeedCelexAsc(version.id()).stream()
        .map(InstrumentEntity::id)
        .toList();
    return provisions.findAllByInstrumentIdInOrderByProvisionKeyAsc(instrumentIds).stream()
        .anyMatch(row -> provisionKey.equals(row.provisionKey()));
  }

  private CorpusView view(CorpusVersionEntity version) {
    List<InstrumentEntity> instrumentRows =
        instruments.findAllByCorpusVersionIdOrderBySeedCelexAsc(version.id());
    List<UUID> instrumentIds = instrumentRows.stream().map(InstrumentEntity::id).toList();
    List<CorpusRelationshipEntity> links = relationships.findAllByCorpusVersionId(version.id());
    return new CorpusView(
        version.versionHash(),
        instrumentRows.stream().map(row -> new InstrumentView(
            row.seedCelex(),
            row.consolidationCelex(),
            row.title(),
            row.textHash(),
            row.consolidationDate(),
            row.applicationFrom())).toList(),
        provisions.findAllByInstrumentIdInOrderByProvisionKeyAsc(instrumentIds).stream()
            .map(row -> new ProvisionView(
                row.provisionKey(),
                row.article(),
                row.paragraph(),
                row.point(),
                row.annex(),
                row.textExcerpt(),
                row.forceStatus().name(),
                row.forceFrom(),
                row.scopeNote()))
            .toList(),
        guidanceDocs.findAllByCorpusVersionIdOrderBySourceKeyAsc(version.id()).stream()
            .map(row -> new GuidanceView(
                row.sourceKey(),
                row.title(),
                row.authorityRank(),
                row.body(),
                links.stream()
                    .filter(link -> row.sourceKey().equals(link.fromKey()))
                    .map(CorpusRelationshipEntity::relation)
                    .findFirst()
                    .orElse("interprets"),
                links.stream()
                    .filter(link -> row.sourceKey().equals(link.fromKey()))
                    .map(CorpusRelationshipEntity::toKey)
                    .findFirst()
                    .orElse(null)))
            .toList(),
        CorpusIngestService.ATTRIBUTION);
  }

  public record CorpusView(
      String corpusVersion,
      List<InstrumentView> instruments,
      List<ProvisionView> provisions,
      List<GuidanceView> guidance,
      String attribution) {
  }

  public record InstrumentView(
      String seedCelex,
      String consolidationCelex,
      String title,
      String textHash,
      LocalDate consolidationDate,
      LocalDate applicationFrom) {
  }

  public record ProvisionView(
      String provisionKey,
      String article,
      String paragraph,
      String point,
      String annex,
      String textExcerpt,
      String forceStatus,
      LocalDate forceFrom,
      String scopeNote) {
  }

  public record GuidanceView(
      String sourceKey,
      String title,
      String authorityRank,
      String body,
      String relation,
      String interpretsProvisionKey) {
  }
}
