package os.assurance.eu.api.sector;

/**
 * Declarative control overlay contributed by a {@link SectorPack}.
 * Codes are seeded into the global controls catalog and attached when the
 * system's sector matches the pack.
 */
public record SectorControlDef(
    String code,
    String name,
    String description,
    String appliesToRiskClass,
    String category) {
}
