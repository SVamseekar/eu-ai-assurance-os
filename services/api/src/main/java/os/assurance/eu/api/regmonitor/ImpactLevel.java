package os.assurance.eu.api.regmonitor;

/**
 * Heuristic impact confidence. v1 prefers {@link #UNCERTAIN} always.
 */
public enum ImpactLevel {
  UNCERTAIN,
  POSSIBLE,
  LIKELY
}
